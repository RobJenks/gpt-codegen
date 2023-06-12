import 'BpmnApp.css'
import 'bpmn-js.css'
import 'diagram-js.css'
// import 'https://unpkg.com/bpmn-js@13.1.0/dist/assets/bpmn-js.css'
  
import React, { createRef, useEffect, useRef } from 'react';
import axios, * as Axios from "axios"
import { SplitView } from 'components/util/splitView'
import { SplitViewVertical } from 'components/util/splitViewVertical'
import * as Events from 'types/events';
import { useQuery } from "react-query";
import ReactBpmn from 'react-bpmn';
import BpmnModeler from 'bpmn-js/dist/bpmn-modeler.production.min.js';


export const BpmnApp: React.FunctionComponent<{}> = () => {
  const initialBpmnContent = '<?xml version="1.0" encoding="UTF-8" standalone="no"?><definitions xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="definitions_21adcbca-143a-4389-b583-601fdf8ed0e7" targetNamespace="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"><process id="GeneratedProcess" isExecutable="true"><startEvent id="Start" name="Start"><outgoing>sequenceFlow_f70120a0-5418-486a-82b1-a8711db61407</outgoing></startEvent><endEvent id="End" name="End"><incoming>sequenceFlow_f70120a0-5418-486a-82b1-a8711db61407</incoming></endEvent><sequenceFlow id="sequenceFlow_f70120a0-5418-486a-82b1-a8711db61407" sourceRef="Start" targetRef="End"/></process><bpmndi:BPMNDiagram id="BPMNDiagram_e89723ad-4a66-4f93-af81-878bf746cbcb"><bpmndi:BPMNPlane bpmnElement="process" id="BPMNPlane_221a40c1-cf7c-4691-ac64-f6d0c2719052"><bpmndi:BPMNShape bpmnElement="Start" id="BPMNShape_2f3a4646-0217-4e69-8e0a-4e815be52bbf"><dc:Bounds height="36.0" width="36.0" x="100.0" y="100.0"/></bpmndi:BPMNShape><bpmndi:BPMNShape bpmnElement="End" id="BPMNShape_44fc7b09-8869-47d4-9bf7-4f3e1a06583c"><dc:Bounds height="36.0" width="36.0" x="186.0" y="100.0"/></bpmndi:BPMNShape><bpmndi:BPMNEdge bpmnElement="sequenceFlow_f70120a0-5418-486a-82b1-a8711db61407" id="BPMNEdge_66da1361-0224-4053-8706-7bd9738a70db"><di:waypoint x="136.0" y="118.0"/><di:waypoint x="186.0" y="118.0"/></bpmndi:BPMNEdge></bpmndi:BPMNPlane></bpmndi:BPMNDiagram></definitions>';

  const targetUrl = 'http://localhost:8080';
  const [initialized, setInitialized] = React.useState(false);
  const [prompt, setPrompt] = React.useState("");
  const [sessionId, setSessionId] = React.useState("abc");
  const [temperature, setTemperature] = React.useState(0.7);
  const [requestWasValid, setRequestWasValid] = React.useState(true);
  const [eventLogData, setEventLogData] = React.useState("");
  const [tokenCompressionPc, setTokenCompressionPc] = React.useState(0.0);
  const [validationStatus, setValidationStatus] = React.useState("");
  const [validationStatusStyle, setValidationStatusStyle] = React.useState("validationStatusSuccess");
  const [validationErrorsTooltip, setValidationErrorsTooltip] = React.useState("");
  const [validationResultsVisible, setValidationResultsVisible] = React.useState(false);
  const [validationResultsVisibilityStyle, setValidationResultsVisibilityStyle] = React.useState("contentHidden");
  const [bpmnViewer, setBpmnViewer] = React.useState<any>(null);
  const bpmnContainerRef = createRef<HTMLDivElement>();
  
  const [state, setState] = React.useState<Events.SessionState>({
    id: "0",
    executionContext: "None",
    events: [],
    totalTokensUsed: 0,
    estimatedCompressedTokenSize: 0,
    estimatedUncompressedTokenSize: 0,
    lastPrompt: "",
    lastResponse: "def run() {\n    \n}",
    validOutput: true,
    validationErrors: [],
    iterationsRequired: 0,
    currentTemperature: 0.7,
    transformedContent: initialBpmnContent
  });


  const processGenerateResponse = (data: Axios.AxiosResponse<Events.SessionState>) => {
    const state = data?.data;
    if (!state) return;
    
    setState(state);
    updateBpmnViewerContent(state.transformedContent);
  }

  const { refetch: generateNewResponse, isLoading: isNewResponseLoading, isFetching: isNewResponseFetching } =
    useQuery<Axios.AxiosResponse<Events.SessionState>, Error>('generateNewResponse', 
      () => axios.post(targetUrl + '/api/gpt/session/' + sessionId + '/prompt/bpmn', 
      {
        "prompt": prompt,
        "temperature": temperature ?? 0.7
      }), 
      { enabled: false, retry: 0, 
        onSuccess: processGenerateResponse, 
        onError: (err) => console.log("Error generating new response: " + err)});

      
  const promptTextUpdated = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
      setPrompt(e.target.value);
  }

  const onInit = useEffect(() => {
    if (initialized) return;
    setInitialized(true);

    const n = Math.floor(Math.random() * 100000000);
    setSessionId(n.toString());
    console.log("Starting new session " + n.toString())

    if (bpmnViewer == null) {
      const container = bpmnContainerRef.current;
      setBpmnViewer(new BpmnModeler({ container }));
    }
  }, []);

  const onBpmnViewerComponentChanged = useEffect(() => {
    updateBpmnViewerContent(state.transformedContent);
  }, [bpmnViewer])

  function updateBpmnViewerContent(content: string) {
    if (bpmnViewer && content) {
      bpmnViewer.importXML(content);
    }
  }

  const onInputKeyPress = (e : React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && e.ctrlKey) {
      generateNewResponse();
    }
  };

  const temperatureChanged = (e: React.ChangeEvent<HTMLInputElement>) => {
    setTemperature(+e.currentTarget.value);
  }

  const lastResponseUpdated = useEffect(() => {
    if (state.lastResponse === "NO") {
      setRequestWasValid(false);
      console.log("Invalid response");
    } 
    else {
      setRequestWasValid(true);
    }
  }, 
  [state.lastResponse])

  const eventLogUpdated = useEffect(() => {
    var content : string = '';
    state.events.reverse().forEach((event) => {
      content += (event.role + ': ' + event.content + '\n');
    })

    setEventLogData(content);
  }, 
  [state.events])

  const totalTokensUsedUpdated = useEffect(() => {
    setTokenCompressionPc(state.estimatedUncompressedTokenSize === 0 ? 0.0 : 
      (1.0 - (state.estimatedCompressedTokenSize / state.estimatedUncompressedTokenSize)));
  },
  [state.totalTokensUsed, state.estimatedUncompressedTokenSize, state.estimatedCompressedTokenSize]);

  const validationResultsUpdated = useEffect(() => {
    setValidationResultsVisible(state.iterationsRequired !== 0);

    setValidationStatus(state.validOutput ? "Valid" : "INVALID");
    setValidationStatusStyle(state.validOutput ? "validationStatusSuccess" : "validationStatusFailure");

    var messagesText = "";
    state.validationErrors.forEach((s, i) => messagesText += ((i === 0 ? "" : "\n") + s));
    setValidationErrorsTooltip(messagesText);    
  },
  [state.validOutput, state.validationErrors, state.iterationsRequired]);

  const validationResultsVisibilityChanged = useEffect(() => {
    setValidationResultsVisibilityStyle(validationResultsVisible ? "contentVisible" : "contentHidden");
  }, [validationResultsVisible])

  return (
      <div style={{ width: "100%", height: "100%", overflow: "hidden" }}>
          {<SplitViewVertical
              top=
                {<SplitView
                  left={ 
                    <div className='paneContainer' style={{height: "100%"}}>
                      <h3>BPMN generation sample</h3>
                      <table style={{width: "100%", height: "95%"}}><tbody>
                        <tr><td colSpan={2}>
                          <div ref={bpmnContainerRef} style={{ width: "100%", height: "400px" }}></div>
                          <br/>
                        </td></tr>
                        <tr><td colSpan={2}><label htmlFor="prompt">What would you like to do?</label><br/></td></tr>
                        <tr>
                          <td>
                            <textarea id="prompt" style={{width: "100%", resize: "both", minHeight: "50px", maxHeight: "150px"}} onChange={promptTextUpdated} onKeyPress={onInputKeyPress}></textarea>
                          </td>
                          <td style={{width: "110px", textAlign: "right"}}>
                            <button disabled={isNewResponseLoading || isNewResponseFetching} onClick={() => generateNewResponse()}>
                              {`${(isNewResponseLoading || isNewResponseFetching) ? "Generating..." : "Update Process"}`}</button>
                        </td></tr>
                        <tr><td>
                          <div style={!requestWasValid ? {} : { visibility: "hidden"}}>
                            <div style={{color:"red"}}>That's not a valid request, please make sure you're requesting a sensible model change</div>
                          </div>
                        </td></tr>
                      </tbody></table>
                    </div>
                  }
                  
                  right={
                  <div className='paneContainer'>
                    <h3>Status</h3>
                    <table><tbody>
                        <tr><td><b>Session ID</b></td><td>{sessionId}</td></tr>
                        <tr><td><b>Temperature</b></td><td><input type="number" min={0.0} max={1.0} step={0.1} value={temperature} onChange={temperatureChanged}/></td></tr>
                        <tr><td><br/></td></tr>
                        <tr><td><b>Actual tokens used</b></td><td>{state.totalTokensUsed}</td></tr>
                        <tr><td><b>Estimated compressed tokens</b></td><td>{state.estimatedCompressedTokenSize}</td></tr>
                        <tr><td><b>Estimated uncompressed tokens</b></td><td>{state.estimatedUncompressedTokenSize}</td></tr>
                        <tr><td><b>Token saving</b></td><td>{(tokenCompressionPc * 100.0).toFixed(1).toString() + '%'}</td></tr>
                        <tr><td><br/></td></tr>
                        <tr><td><b>Code validity check</b></td><td><div className={`${validationResultsVisibilityStyle} ${validationStatusStyle}`}>{validationStatus}</div></td></tr>
                        <tr><td><b>Code validation results</b></td><td><div className={validationResultsVisibilityStyle}>
                          {state?.validationErrors?.length ?? 0} messages&nbsp;
                          <u className={`${(state?.validationErrors?.length ?? 0) === 0 ? 'contentHidden' : 'contentVisible'}`} title={validationErrorsTooltip} style={{color: 'gray', cursor: 'pointer'}}>[?]</u></div>
                        </td></tr>
                        <tr><td><b>Iterations performed</b></td><td className={validationResultsVisibilityStyle}>{state.iterationsRequired}</td></tr>
                    </tbody></table>
                  </div>
                  }

                />}

              bottom={
              <div className='paneContainer' style={{marginLeft: "8px", marginRight: "8px"}}>
                <h3>Event log</h3>
                <textarea className="eventLog" style={{width: "97.5vw"}} rows={16} value={eventLogData} readOnly={true}></textarea>
              </div>} 
          />}


        </div>
  );
}

export default BpmnApp;