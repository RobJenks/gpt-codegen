// https://blog.theodo.com/2020/11/react-resizeable-split-panels/
// https://codesandbox.io/s/splitview-p37yw?file=/src/components/SplitView.tsx

import React, { createRef, useEffect, useState } from "react";
import 'components/util/splitViewVertical.css'

const MIN_HEIGHT = 75;

interface SplitViewVerticalProps {
  top: React.ReactElement;
  bottom: React.ReactElement;
  className?: string;
}

const TopPane: React.FunctionComponent<{
  topHeight: number | undefined;
  setTopHeight: (value: number) => void;
}> = ({ children, topHeight, setTopHeight }) => {
  const topRef = createRef<HTMLDivElement>();

  useEffect(() => {
    if (topRef.current) {
      if (!topHeight) {
        setTopHeight(topRef.current.clientHeight);
        return;
      }
      
      topRef.current.style.height = `${topHeight}px`;
    }
  }, [topRef, topHeight, setTopHeight]);

  return <div className="verticalTopPane" ref={topRef}>{children}</div>;
};

export const SplitViewVertical: React.FunctionComponent<SplitViewVerticalProps> = ({
  top,
  bottom,
  className
}) => {
  const [topHeight, setTopHeight] = useState<undefined | number>(undefined);
  const [separatorYPosition, setSeparatorYPosition] = useState<
    undefined | number
  >(undefined);
  const [dragging, setDragging] = useState(false);

  const splitPaneRef = createRef<HTMLDivElement>();

  const onMouseDown = (e: React.MouseEvent) => {
    setSeparatorYPosition(e.clientY);
    setDragging(true);
  };

  const onTouchStart = (e: React.TouchEvent) => {
    setSeparatorYPosition(e.touches[0].clientY);
    setDragging(true);
  };

  const onMove = (clientY: number) => {
    if (dragging && topHeight && separatorYPosition) {
      const newTopHeight = topHeight + clientY - separatorYPosition;
      setSeparatorYPosition(clientY);

      if (newTopHeight < MIN_HEIGHT) {
        setTopHeight(MIN_HEIGHT);
        return;
      }

      if (splitPaneRef.current) {
        const splitPaneHeight = splitPaneRef.current.clientHeight;

        if (newTopHeight > splitPaneHeight - MIN_HEIGHT) {
          setTopHeight(splitPaneHeight - MIN_HEIGHT);
          return;
        }
      }

      setTopHeight(newTopHeight);
    }
  };

  const onMouseMove = (e: MouseEvent) => {
    e.preventDefault();
    onMove(e.clientY);
  };

  const onTouchMove = (e: TouchEvent) => {
    onMove(e.touches[0].clientY);
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
    <div className="verticalSplitViewContainer">
      <div className={`verticalSplitView ${className ?? ""}`} ref={splitPaneRef}>
        <TopPane topHeight={topHeight} setTopHeight={setTopHeight}>
          {top}
        </TopPane>
        <div
          className="vertical-divider-hitbox"
          onMouseDown={onMouseDown}
          onTouchStart={onTouchStart}
          onTouchEnd={onMouseUp}
        >
          <div className="vertical-divider" />
        </div>
        <div className="verticalBottomPane">{bottom}</div>
      </div>
    </div>
  );
};
