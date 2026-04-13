import React, { useEffect, useRef } from 'react'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/diagram-js.css'
import './BpmnViewer.css'

interface BpmnViewerProps {
  xml?: string
}

const BpmnViewer: React.FC<BpmnViewerProps> = ({ xml }) => {
  const containerRef = useRef<HTMLDivElement>(null)
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const modelerRef = useRef<any>(null)
  const initializedRef = useRef(false)

  useEffect(() => {
    if (initializedRef.current || !containerRef.current) return
    initializedRef.current = true

    // Dynamic import avoids SSR issues and defers the heavy bundle
    import('bpmn-js/dist/bpmn-modeler.production.min.js').then((mod) => {
      const BpmnModeler = mod.default
      modelerRef.current = new BpmnModeler({ container: containerRef.current })
    })

    return () => {
      modelerRef.current?.destroy()
      modelerRef.current = null
      initializedRef.current = false
    }
  }, [])

  useEffect(() => {
    if (!xml) return
    // Poll briefly until the modeler is ready after its async import
    const tryImport = () => {
      if (modelerRef.current) {
        modelerRef.current.importXML(xml).catch(console.error)
      } else {
        setTimeout(tryImport, 50)
      }
    }
    tryImport()
  }, [xml])

  return (
    <div className="bpmn-viewer-wrapper">
      <div ref={containerRef} className="bpmn-viewer-canvas" />
    </div>
  )
}

export default BpmnViewer
