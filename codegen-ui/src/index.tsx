import React from 'react';
import ReactDOM from 'react-dom';
import 'index.css';
import { App } from 'App';
import reportWebVitals from 'reportWebVitals';
import { QueryClient, QueryClientProvider, useQuery } from "react-query";

declare global {
  interface Window { config: any; }
}

const queryClient = new QueryClient();

ReactDOM.render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />      
    </QueryClientProvider>
  </React.StrictMode>,
  document.getElementById('root')
);

reportWebVitals();
