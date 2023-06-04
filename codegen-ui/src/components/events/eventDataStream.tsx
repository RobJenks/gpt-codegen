import React from 'react';
import * as Events from 'types/events';
import 'components/events/eventDataStream.css'

type EventDataStreamProps = {
    data: Events.Data[]
}

class EventDataStream extends React.Component<EventDataStreamProps, {}> {

    render() {
        return (
             <div className="eventTableContainer">
              <table className="eventTable">
                <thead>
                  <tr>
                    <th>Event</th>
                    <th>Data</th>
                  </tr>
                </thead>
                
                <tbody>
                  {
                      this.props.data.map((ev, i) => {
                          return (
                              <tr key={ev.offset}>
                                  <td>{ev.offset}</td>
                                  <td>{JSON.stringify(ev)}</td>
                              </tr>
                          )
                      })
                  }
                </tbody>
              </table>
             </div>
          );
    }
}

export default EventDataStream;
