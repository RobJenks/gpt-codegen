import React, { useEffect, useRef, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { SplitPane } from '../components/layout/SplitPane'
import { SplitPaneV } from '../components/layout/SplitPaneV'
import { generateCode } from '../api/session'
import type { SessionState } from '../types/events'
import './Page.css'

const CodePage: React.FC = () => {
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

  const outputCode = sessionState?.currentIntermediateModelData ?? ''

  const { mutate: submit, isPending } = useMutation({
    mutationFn: () => generateCode(sessionId, { prompt, temperature }),
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

  useEffect(() => {
    if (eventLogRef.current) {
      eventLogRef.current.scrollTop = eventLogRef.current.scrollHeight
    }
  }, [eventLog])

  const onKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') submit()
  }

  const mainPanel = (
    <div className="page-pane">
      <h3 className="pane-title">Generated Code</h3>
      <textarea
        className="code-output"
        value={outputCode}
        readOnly
        rows={18}
        placeholder="Generated code will appear here…"
      />

      <div className="prompt-row">
        <label className="prompt-label" htmlFor="code-prompt">
          What would you like to do?{' '}
          <span className="prompt-hint">(Enter to submit)</span>
        </label>
        <div className="prompt-input-row">
          <input
            id="code-prompt"
            type="text"
            className="prompt-input"
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            onKeyDown={onKeyDown}
            placeholder="Describe the code you want to generate or modify…"
          />
          <button
            className="submit-btn"
            disabled={isPending || !prompt.trim()}
            onClick={() => submit()}
          >
            {isPending ? 'Generating…' : 'Update Code'}
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
        top={<SplitPane left={mainPanel} right={statusPanel} />}
        bottom={eventLogPanel}
      />
    </div>
  )
}

export default CodePage
