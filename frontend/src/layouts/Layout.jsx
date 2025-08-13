import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import ScrollButtons from '../components/ScrollButtons';
import { Outlet } from 'react-router-dom';
import AlarmContainer from "@/components/AlarmContainer.jsx";

export default function Layout() {

    return (
        <>
            <Navbar />
            <article className="flex items-start">
                <Sidebar />
                <main id="content" className="w-full p-8">
                    <Outlet />
                </main>
            </article>
            <ScrollButtons />
            <AlarmContainer />
        </>
    );
}
