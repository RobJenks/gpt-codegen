// https://blog.theodo.com/2020/11/react-resizeable-split-panels/
// https://codesandbox.io/s/splitview-p37yw?file=/src/components/SplitView.tsx

import React, { createRef, useEffect, useState } from "react";
import 'components/util/splitView.css'

const MIN_WIDTH = 75;

interface SplitViewProps {
  left: React.ReactElement;
  right: React.ReactElement;
  className?: string;
}

const LeftPane: React.FunctionComponent<{
  leftWidth: number | undefined;
  setLeftWidth: (value: number) => void;
}> = ({ children, leftWidth, setLeftWidth }) => {
  const leftRef = createRef<HTMLDivElement>();

  useEffect(() => {
    if (leftRef.current) {
      if (!leftWidth) {
        setLeftWidth(leftRef.current.clientWidth);
        return;
      }

      leftRef.current.style.width = `${leftWidth}px`;
    }
  }, [leftRef, leftWidth, setLeftWidth]);

  return <div className="leftPane" ref={leftRef}>{children}</div>;
};

export const SplitView: React.FunctionComponent<SplitViewProps> = ({
  left,
  right,
  className
}) => {
  const [leftWidth, setLeftWidth] = useState<undefined | number>(undefined);
  const [separatorXPosition, setSeparatorXPosition] = useState<
    undefined | number
  >(undefined);
  const [dragging, setDragging] = useState(false);

  const splitPaneRef = createRef<HTMLDivElement>();

  const onMouseDown = (e: React.MouseEvent) => {
    setSeparatorXPosition(e.clientX);
    setDragging(true);
  };

  const onTouchStart = (e: React.TouchEvent) => {
    setSeparatorXPosition(e.touches[0].clientX);
    setDragging(true);
  };

  const onMove = (clientX: number) => {
    if (dragging && leftWidth && separatorXPosition) {
      const newLeftWidth = leftWidth + clientX - separatorXPosition;
      setSeparatorXPosition(clientX);

      if (newLeftWidth < MIN_WIDTH) {
        setLeftWidth(MIN_WIDTH);
        return;
      }

      if (splitPaneRef.current) {
        const splitPaneWidth = splitPaneRef.current.clientWidth;

        if (newLeftWidth > splitPaneWidth - MIN_WIDTH) {
          setLeftWidth(splitPaneWidth - MIN_WIDTH);
          return;
        }
      }

      setLeftWidth(newLeftWidth);
    }
  };

  const onMouseMove = (e: MouseEvent) => {
    e.preventDefault();
    onMove(e.clientX);
  };

  const onTouchMove = (e: TouchEvent) => {
    onMove(e.touches[0].clientX);
  };

  const onMouseUp = () => {
    setDragging(false);
  };

  React.useEffect(() => {
    document.addEventListener("mousemove", onMouseMove);
    document.addEventListener("touchmove", onTouchMove);
    document.addEventListener("mouseup", onMouseUp);

    return () => {
      document.removeEventListener("mousemove", onMouseMove);
      document.removeEventListener("touchmove", onTouchMove);
      document.removeEventListener("mouseup", onMouseUp);
    };
  });

  return (
    <div className="splitViewContainer">
      <div className={`splitView ${className ?? ""}`} ref={splitPaneRef}>
        <LeftPane leftWidth={leftWidth} setLeftWidth={setLeftWidth}>
          {left}
        </LeftPane>
        <div
          className="divider-hitbox"
          onMouseDown={onMouseDown}
          onTouchStart={onTouchStart}
          onTouchEnd={onMouseUp}
        >
          <div className="divider" />
        </div>
        <div className="rightPane">{right}</div>
      </div>
    </div>
  );
};
