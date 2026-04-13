import React, { useEffect, useRef, useState } from 'react'
import './SplitPane.css'

const MIN_WIDTH = 75

interface SplitPaneProps {
  left: React.ReactElement
  right: React.ReactElement
}

export const SplitPane: React.FC<SplitPaneProps> = ({ left, right }) => {
  // undefined = use CSS flex ratio; number = JS pixel width (post-drag)
  const [leftWidth, setLeftWidth] = useState<number | undefined>(undefined)
  const [separatorX, setSeparatorX] = useState<number | undefined>(undefined)
  const [dragging, setDragging] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)
  const leftRef = useRef<HTMLDivElement>(null)

  // Apply explicit pixel width only after the user starts dragging
  useEffect(() => {
    if (leftRef.current && leftWidth !== undefined) {
      leftRef.current.style.width = `${leftWidth}px`
      leftRef.current.style.flex = 'none'
    }
  }, [leftWidth])

  const startDrag = (clientX: number) => {
    // Seed from actual rendered size so the first move is smooth
    const currentWidth = leftRef.current?.offsetWidth
    if (currentWidth !== undefined) setLeftWidth(currentWidth)
    setSeparatorX(clientX)
    setDragging(true)
  }

  const onMove = (clientX: number) => {
    if (!dragging || leftWidth === undefined || separatorX === undefined) return
    const newWidth = leftWidth + clientX - separatorX
    setSeparatorX(clientX)
    if (newWidth < MIN_WIDTH) { setLeftWidth(MIN_WIDTH); return }
    if (containerRef.current) {
      const total = containerRef.current.clientWidth
      if (newWidth > total - MIN_WIDTH) { setLeftWidth(total - MIN_WIDTH); return }
    }
    setLeftWidth(newWidth)
  }

  // Re-register on every render so closures always have fresh state
  useEffect(() => {
    const onMouseMove = (e: MouseEvent) => { e.preventDefault(); onMove(e.clientX) }
    const onTouchMove = (e: TouchEvent) => onMove(e.touches[0].clientX)
    const onMouseUp = () => setDragging(false)
    document.addEventListener('mousemove', onMouseMove)
    document.addEventListener('touchmove', onTouchMove)
    document.addEventListener('mouseup', onMouseUp)
    return () => {
      document.removeEventListener('mousemove', onMouseMove)
      document.removeEventListener('touchmove', onTouchMove)
      document.removeEventListener('mouseup', onMouseUp)
    }
  })

  return (
    <div className="split-pane" ref={containerRef}>
      <div className="split-left" ref={leftRef}>
        {left}
      </div>
      <div
        className="split-divider-h"
        onMouseDown={(e) => startDrag(e.clientX)}
        onTouchStart={(e) => startDrag(e.touches[0].clientX)}
        onTouchEnd={() => setDragging(false)}
      >
        <div className="split-divider-h-bar" />
      </div>
      <div className="split-right">{right}</div>
    </div>
  )
}
