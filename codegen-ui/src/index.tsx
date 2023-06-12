import React from 'react';
import { createRoot } from 'react-dom/client';
import 'index.css';
import { App } from 'App';
import { BpmnApp } from 'BpmnApp';
import reportWebVitals from 'reportWebVitals';
import { QueryClient, QueryClientProvider, useQuery } from "react-query";

declare global {
  interface Window { config: any; }
}

const queryClient = new QueryClient();

const container = document.getElementById('root');
const root = createRoot(container!);

root.render(
    <QueryClientProvider client={queryClient}>
      <BpmnApp />      
    </QueryClientProvider>
);

reportWebVitals();
