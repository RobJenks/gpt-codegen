import React, { useEffect, useRef, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { SplitPane } from '../components/layout/SplitPane'
import { SplitPaneV } from '../components/layout/SplitPaneV'
import BpmnViewer from '../components/bpmn/BpmnViewer'
import { generateBpmn } from '../api/session'
import type { SessionState } from '../types/events'
import './Page.css'

const INITIAL_BPMN =
  '<?xml version="1.0" encoding="UTF-8" standalone="no"?>' +
  '<definitions xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" ' +
  'xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" ' +
  'xmlns:di="http://www.omg.org/spec/DD/20100524/DI" ' +
  'id="definitions_1" targetNamespace="http://www.omg.org/spec/BPMN/20100524/MODEL" ' +
  'xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">' +
  '<process id="GeneratedProcess" isExecutable="true">' +
  '<startEvent id="Start" name="Start"><outgoing>sf1</outgoing></startEvent>' +
  '<endEvent id="End" name="End"><incoming>sf1</incoming></endEvent>' +
  '<sequenceFlow id="sf1" sourceRef="Start" targetRef="End"/>' +
  '</process>' +
  '<bpmndi:BPMNDiagram id="diagram1">' +
  '<bpmndi:BPMNPlane bpmnElement="GeneratedProcess" id="plane1">' +
  '<bpmndi:BPMNShape bpmnElement="Start" id="shape_Start"><dc:Bounds height="36" width="36" x="100" y="100"/></bpmndi:BPMNShape>' +
  '<bpmndi:BPMNShape bpmnElement="End" id="shape_End"><dc:Bounds height="36" width="36" x="220" y="100"/></bpmndi:BPMNShape>' +
  '<bpmndi:BPMNEdge bpmnElement="sf1" id="edge_sf1"><di:waypoint x="136" y="118"/><di:waypoint x="220" y="118"/></bpmndi:BPMNEdge>' +
  '</bpmndi:BPMNPlane>' +
  '</bpmndi:BPMNDiagram>' +
  '</definitions>'

const BpmnPage: React.FC = () => {
  const [sessionId] = useState(() => Math.floor(Math.random() * 100_000_000).toString())
  const [prompt, setPrompt] = useState('')
  const [temperature, setTemperature] = useState(0.7)
  const [sessionState, setSessionState] = useState<SessionState | null>(null)
  const [errorMessage, setErrorMessage] = useState('')
  const [eventLog, setEventLog] = useState('')
  const eventLogRef = useRef<HTMLTextAreaElement>(null)

  const tokenCompressionPc =
    sessionState?.estimatedUncompressedTokenSize && sessionState.estimatedUncompressedTokenSize > 0
      ? 1.0 -
        (sessionState.estimatedCompressedTokenSize ?? 0) /
          sessionState.estimatedUncompressedTokenSize
      : 0.0

  const { mutate: submit, isPending } = useMutation({
    mutationFn: () => generateBpmn(sessionId, { prompt, temperature }),
    onSuccess: (data) => {
      setSessionState(data)
      setErrorMessage('')
      if (data.events) {
        const lines = [...data.events]
          .reverse()
          .map((e) => `${e.role}: ${e.content}`)
          .join('\n')
        setEventLog(lines)
      }
    },
    onError: (err) => {
      setErrorMessage('Request failed: ' + String(err))
      console.error(err)
    },
  })

  // Auto-scroll event log to bottom
  useEffect(() => {
    if (eventLogRef.current) {
      eventLogRef.current.scrollTop = eventLogRef.current.scrollHeight
    }
  }, [eventLog])

  const onKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && e.ctrlKey) submit()
  }

  const bpmnXml = sessionState?.currentBpmnData || INITIAL_BPMN

  const mainPanel = (
    <div className="page-pane">
      <BpmnViewer xml={bpmnXml} />

      <div className="prompt-row">
        <label className="prompt-label" htmlFor="bpmn-prompt">
          What would you like to do?{' '}
          <span className="prompt-hint">(Ctrl+Enter to submit)</span>
        </label>
        <div className="prompt-input-row">
          <textarea
            id="bpmn-prompt"
            className="prompt-textarea"
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            onKeyDown={onKeyDown}
            rows={3}
            placeholder="Describe the process you want to generate or modify…"
          />
          <button
            className="submit-btn"
            disabled={isPending || !prompt.trim()}
            onClick={() => submit()}
          >
            {isPending ? 'Generating…' : 'Update Process'}
          </button>
        </div>
        {errorMessage && <div className="error-msg">{errorMessage}</div>}
      </div>
    </div>
  )

  const statusPanel = (
    <div className="status-pane">
      <h3 className="pane-title">Status</h3>
      <table className="status-table">
        <tbody>
          <tr>
            <td className="stat-label">Session ID</td>
            <td className="stat-value mono">{sessionId}</td>
          </tr>
          <tr>
            <td className="stat-label">Temperature</td>
            <td className="stat-value">
              <input
                type="number"
                min={0}
                max={1}
                step={0.1}
                value={temperature}
                onChange={(e) => setTemperature(+e.target.value)}
                className="temp-input"
              />
            </td>
          </tr>
          <tr><td colSpan={2}><div className="stat-divider" /></td></tr>
          <tr>
            <td className="stat-label">Actual tokens</td>
            <td className="stat-value">{sessionState?.totalTokensUsed ?? 0}</td>
          </tr>
          <tr>
            <td className="stat-label">Compressed est.</td>
            <td className="stat-value">{sessionState?.estimatedCompressedTokenSize ?? 0}</td>
          </tr>
          <tr>
            <td className="stat-label">Uncompressed est.</td>
            <td className="stat-value">{sessionState?.estimatedUncompressedTokenSize ?? 0}</td>
          </tr>
          <tr>
            <td className="stat-label">Token saving</td>
            <td className="stat-value">{(tokenCompressionPc * 100).toFixed(1)}%</td>
          </tr>
          <tr><td colSpan={2}><div className="stat-divider" /></td></tr>
          <tr>
            <td className="stat-label">Validity</td>
            <td className="stat-value">
              {sessionState?.validOutput != null ? (
                <span className={sessionState.validOutput ? 'valid-ok' : 'valid-fail'}>
                  {sessionState.validOutput ? 'Valid' : 'INVALID'}
                </span>
              ) : (
                <span className="stat-na">—</span>
              )}
            </td>
          </tr>
          <tr>
            <td className="stat-label">Validation msgs</td>
            <td className="stat-value">
              {sessionState?.validationErrors?.length ?? 0}
              {(sessionState?.validationErrors?.length ?? 0) > 0 && (
                <span
                  className="tooltip-link"
                  title={sessionState?.validationErrors?.join('\n')}
                >
                  {' '}[?]
                </span>
              )}
            </td>
          </tr>
          <tr>
            <td className="stat-label">Iterations</td>
            <td className="stat-value">{sessionState?.iterationsRequired ?? 0}</td>
          </tr>
        </tbody>
      </table>
    </div>
  )

  const eventLogPanel = (
    <div className="event-log-pane">
      <h3 className="pane-title">Event Log</h3>
      <textarea
        ref={eventLogRef}
        className="event-log-area"
        value={eventLog}
        readOnly
        rows={10}
      />
    </div>
  )

  return (
    <div className="page-root">
      <SplitPaneV
        top={
          <SplitPane left={mainPanel} right={statusPanel} />
        }
        bottom={eventLogPanel}
      />
    </div>
  )
}

export default BpmnPage
