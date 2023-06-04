
import React, { useEffect, useState } from "react";

interface CodeAreaProps {
    code: string;
}

export const CodeArea: React.FunctionComponent<CodeAreaProps> = ({code}) => {
    const [text, setText] = useState<string>("");
    
    useEffect(() => {
        setText(code);
    });
  
  return (
    <textarea className="outputCode" id="output" rows={16} cols={120} value={code} readOnly={true}></textarea>
  );
};
