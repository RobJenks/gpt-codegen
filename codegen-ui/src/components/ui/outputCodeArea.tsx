import React from 'react';

class OutputCodeArea extends React.Component<React.HTMLProps<HTMLTextAreaElement>, {value: string}> {
    render() {
        const [value, setValue] = React.useState("abc123");
        return <textarea {...this.props} value={this.state.value}/>;
    }

    // componentDidMount(): void {
    //     setValue("abc123");
    // }
}