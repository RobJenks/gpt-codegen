export {}
// import 'BpmnApp.css'
// import React, { useEffect } from 'react';
// import axios, * as Axios from "axios"
// import { SplitView } from 'components/util/splitView'
// import { SplitViewVertical } from 'components/util/splitViewVertical'
// import * as Events from 'types/events';
// import { useQuery } from "react-query";
// import ReactBpmn from 'react-bpmn';


// export const BpmnApp: React.FunctionComponent<{}> = () => {
//   const targetUrl = 'http://localhost:8080';
//   const [prompt, setPrompt] = React.useState("");
//   const [sessionId, setSessionId] = React.useState("abc");
//   const [temperature, setTemperature] = React.useState(0.7);
//   const [requestWasValid, setRequestWasValid] = React.useState(true);
//   const [eventLogData, setEventLogData] = React.useState("");
//   const [tokenCompressionPc, setTokenCompressionPc] = React.useState(0.0);
//   const [validationStatus, setValidationStatus] = React.useState("");
//   const [validationStatusStyle, setValidationStatusStyle] = React.useState("validationStatusSuccess");
//   const [validationErrorsTooltip, setValidationErrorsTooltip] = React.useState("");
//   const [validationResultsVisible, setValidationResultsVisible] = React.useState(false);
//   const [validationResultsVisibilityStyle, setValidationResultsVisibilityStyle] = React.useState("contentHidden");
//   const [bpmnPath, setBpmnPath] = React.useState("initial.bpmn");
  
//   const [state, setState] = React.useState<Events.SessionState>({
//     id: "0",
//     executionContext: "None",
//     events: [],
//     totalTokensUsed: 0,
//     estimatedCompressedTokenSize: 0,
//     estimatedUncompressedTokenSize: 0,
//     lastPrompt: "",
//     lastResponse: "def run() {\n    \n}",
//     validOutput: true,
//     validationErrors: [],
//     iterationsRequired: 0,
//     currentTemperature: 0.7,
//     transformedContent: ""
//   });


//   const processGenerateResponse = (data: Axios.AxiosResponse<Events.SessionState>) => {
//     const state = data?.data;
//     if (!state) return;
    
//     setState(state);
    
//     const newBpmnPath = '../../codegen-service/generated/bpmn/model-' + state.id + ".bpmn";
//     if (newBpmnPath !== bpmnPath) {
//       console.log("Setting new BPMN path: " + bpmnPath);
//       setBpmnPath(newBpmnPath);
//     }
//   }

//   useEffect(() => {
//     console.log("New bpmn path: " + bpmnPath);
//   }, [bpmnPath])
  
//   const { refetch: generateNewResponse, isLoading: isNewResponseLoading, isFetching: isNewResponseFetching } =
//     useQuery<Axios.AxiosResponse<Events.SessionState>, Error>('generateNewResponse', 
//       () => axios.post(targetUrl + '/api/gpt/session/' + sessionId + '/prompt/bpmn', 
//       {
//         "prompt": prompt,
//         "temperature": temperature ?? 0.7
//       }), 
//       { enabled: false, retry: 0, 
//         onSuccess: processGenerateResponse, 
//         onError: (err) => console.log("Error generating new response: " + err)});

      
//   const promptTextUpdated = (e: React.ChangeEvent<HTMLInputElement>) => {
//       setPrompt(e.target.value);
//   }

//   const onInit = useEffect(() => {
//     const n = Math.floor(Math.random() * 100000000);
//     setSessionId(n.toString());
//     console.log("Starting new session " + n.toString())
//   }, []);

//   const onInputKeyPress = (e : React.KeyboardEvent<HTMLInputElement>) => {
//     if (e.key === "Enter") {
//       generateNewResponse();
//     }
//   };

//   const temperatureChanged = (e: React.ChangeEvent<HTMLInputElement>) => {
//     setTemperature(+e.currentTarget.value);
//   }

//   const lastResponseUpdated = useEffect(() => {
//     if (state.lastResponse === "NO") {
//       setRequestWasValid(false);
//       console.log("Invalid response");
//     } 
//     else {
//       setRequestWasValid(true);
//     }
//   }, 
//   [state.lastResponse])

//   const eventLogUpdated = useEffect(() => {
//     var content : string = '';
//     state.events.reverse().forEach((event) => {
//       content += (event.role + ': ' + event.content + '\n');
//     })

//     setEventLogData(content);
//   }, 
//   [state.events])

//   const totalTokensUsedUpdated = useEffect(() => {
//     setTokenCompressionPc(state.estimatedUncompressedTokenSize === 0 ? 0.0 : 
//       (1.0 - (state.estimatedCompressedTokenSize / state.estimatedUncompressedTokenSize)));
//   },
//   [state.totalTokensUsed, state.estimatedUncompressedTokenSize, state.estimatedCompressedTokenSize]);

//   const validationResultsUpdated = useEffect(() => {
//     setValidationResultsVisible(state.iterationsRequired !== 0);

//     setValidationStatus(state.validOutput ? "Valid" : "INVALID");
//     setValidationStatusStyle(state.validOutput ? "validationStatusSuccess" : "validationStatusFailure");

//     var messagesText = "";
//     state.validationErrors.forEach((s, i) => messagesText += ((i === 0 ? "" : "\n") + s));
//     setValidationErrorsTooltip(messagesText);    
//   },
//   [state.validOutput, state.validationErrors, state.iterationsRequired]);

//   const validationResultsVisibilityChanged = useEffect(() => {
//     setValidationResultsVisibilityStyle(validationResultsVisible ? "contentVisible" : "contentHidden");
//   }, [validationResultsVisible])

//   function onBpmnShown() {
//     console.log("*** Showing BPMN");
//   }

//   function onBpmnLoading() {
//     console.log("*** Loading BPMN");
//   }

//   function onBpmnError(err: any) {
//     console.log("*** Error showing BPMN:" + err);
//   }

//   return (
//       <div style={{ width: "100%", height: "100%", overflow: "hidden" }}>
//           {<SplitViewVertical
//               top=
//                 {<SplitView
//                   left={ 
//                     <div className='paneContainer' style={{height: "100%"}}>
//                       <h3>BPMN generation sample</h3>
//                       <table style={{width: "100%", height: "95%"}}><tbody>
//                         <tr><td colSpan={2}>
//                           <ReactBpmn url={bpmnPath} style={{width: "100%"}}
//                             onShown={ onBpmnShown }
//                             onLoading={ onBpmnLoading }
//                             onError={ onBpmnError } />
//                           <br/>
//                         </td></tr>
//                         <tr><td colSpan={2}><label htmlFor="prompt">What would you like to do?</label><br/></td></tr>
//                         <tr>
//                           <td>
//                             <input type="text" id="prompt" style={{width: "100%"}} onChange={promptTextUpdated} onKeyPress={onInputKeyPress}></input>
//                           </td>
//                           <td style={{width: "110px", textAlign: "right"}}>
//                             <button disabled={isNewResponseLoading || isNewResponseFetching} onClick={() => generateNewResponse()}>
//                               {`${(isNewResponseLoading || isNewResponseFetching) ? "Generating..." : "Update Process"}`}</button>
//                         </td></tr>
//                         <tr><td>
//                           <div style={!requestWasValid ? {} : { visibility: "hidden"}}>
//                             <div style={{color:"red"}}>That's not a valid request, please make sure you're requesting a sensible model change</div>
//                           </div>
//                         </td></tr>
//                       </tbody></table>
//                     </div>
//                   }
                  
//                   right={
//                   <div className='paneContainer'>
//                     <h3>Status</h3>
//                     <table><tbody>
//                         <tr><td><b>Session ID</b></td><td>{sessionId}</td></tr>
//                         <tr><td><b>Temperature</b></td><td><input type="number" min={0.0} max={1.0} step={0.1} value={temperature} onChange={temperatureChanged}/></td></tr>
//                         <tr><td><br/></td></tr>
//                         <tr><td><b>Actual tokens used</b></td><td>{state.totalTokensUsed}</td></tr>
//                         <tr><td><b>Estimated compressed tokens</b></td><td>{state.estimatedCompressedTokenSize}</td></tr>
//                         <tr><td><b>Estimated uncompressed tokens</b></td><td>{state.estimatedUncompressedTokenSize}</td></tr>
//                         <tr><td><b>Token saving</b></td><td>{(tokenCompressionPc * 100.0).toFixed(1).toString() + '%'}</td></tr>
//                         <tr><td><br/></td></tr>
//                         <tr><td><b>Code validity check</b></td><td><div className={`${validationResultsVisibilityStyle} ${validationStatusStyle}`}>{validationStatus}</div></td></tr>
//                         <tr><td><b>Code validation results</b></td><td><div className={validationResultsVisibilityStyle}>
//                           {state?.validationErrors?.length ?? 0} messages&nbsp;
//                           <u className={`${(state?.validationErrors?.length ?? 0) === 0 ? 'contentHidden' : 'contentVisible'}`} title={validationErrorsTooltip} style={{color: 'gray', cursor: 'pointer'}}>[?]</u></div>
//                         </td></tr>
//                         <tr><td><b>Iterations performed</b></td><td className={validationResultsVisibilityStyle}>{state.iterationsRequired}</td></tr>
//                     </tbody></table>
//                   </div>
//                   }

//                 />}

//               bottom={
//               <div className='paneContainer' style={{marginLeft: "8px", marginRight: "8px"}}>
//                 <h3>Event log</h3>
//                 <textarea className="eventLog" style={{width: "97.5vw"}} rows={16} value={eventLogData} readOnly={true}></textarea>
//               </div>} 
//           />}


//         </div>
//   );
// }

// export default BpmnApp;