import React, { createRef, useEffect, useState } from 'react'
import './SplitPaneV.css'

const MIN_HEIGHT = 75

interface SplitPaneVProps {
  top: React.ReactElement
  bottom: React.ReactElement
}

const TopPane: React.FC<{
  children: React.ReactNode
  topHeight: number | undefined
  setTopHeight: (value: number) => void
}> = ({ children, topHeight, setTopHeight }) => {
  const ref = createRef<HTMLDivElement>()

  useEffect(() => {
    if (!ref.current) return
    if (!topHeight) {
      setTopHeight(ref.current.clientHeight)
      return
    }
    ref.current.style.height = `${topHeight}px`
  }, [ref, topHeight, setTopHeight])

  return (
    <div className="split-top" ref={ref}>
      {children}
    </div>
  )
}

export const SplitPaneV: React.FC<SplitPaneVProps> = ({ top, bottom }) => {
  const [topHeight, setTopHeight] = useState<number | undefined>(undefined)
  const [separatorY, setSeparatorY] = useState<number | undefined>(undefined)
  const [dragging, setDragging] = useState(false)
  const containerRef = createRef<HTMLDivElement>()

  const onMouseDown = (e: React.MouseEvent) => {
    setSeparatorY(e.clientY)
    setDragging(true)
  }

  const onTouchStart = (e: React.TouchEvent) => {
    setSeparatorY(e.touches[0].clientY)
    setDragging(true)
  }

  const onMove = (clientY: number) => {
    if (!dragging || !topHeight || !separatorY) return
    const newHeight = topHeight + clientY - separatorY
    setSeparatorY(clientY)
    if (newHeight < MIN_HEIGHT) { setTopHeight(MIN_HEIGHT); return }
    if (containerRef.current) {
      const total = containerRef.current.clientHeight
      if (newHeight > total - MIN_HEIGHT) { setTopHeight(total - MIN_HEIGHT); return }
    }
    setTopHeight(newHeight)
  }

  useEffect(() => {
    const onMouseMove = (e: MouseEvent) => { e.preventDefault(); onMove(e.clientY) }
    const onTouchMove = (e: TouchEvent) => onMove(e.touches[0].clientY)
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
    <div className="split-pane-v" ref={containerRef}>
      <TopPane topHeight={topHeight} setTopHeight={setTopHeight}>
        {top}
      </TopPane>
      <div
        className="split-divider-v"
        onMouseDown={onMouseDown}
        onTouchStart={onTouchStart}
        onTouchEnd={() => setDragging(false)}
      >
        <div className="split-divider-v-bar" />
      </div>
      <div className="split-bottom">{bottom}</div>
    </div>
  )
}
