// main.jsx
import React, {useEffect} from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import {applyStoredTheme} from "./functions/index.js";

    applyStoredTheme();

ReactDOM.createRoot(document.getElementById('root')).render(
    //<React.StrictMode>
        <BrowserRouter>
            <App />
        </BrowserRouter>
    //</React.StrictMode>
);


