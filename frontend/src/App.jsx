
// App.jsx
import { Routes, Route } from 'react-router-dom';
import Layout from './layouts/Layout';
import Journal from './pages/Journal';
import Login from "@/pages/Login.jsx";
//import Supplement from './pages/Supplement';
//import Home from './pages/Home';

export default function App() {
    return (
        <Routes>
            <Route path="/" element={<Layout />}>
                <Route path="/login" element={<Login />}></Route>
                <Route path="/journal" element={<Journal />}></Route>
                {/* 필요한 페이지 계속 추가 */}
                <Route path="*" element={<div>404 - 페이지 없음</div>} />
            </Route>
        </Routes>
    );
}
