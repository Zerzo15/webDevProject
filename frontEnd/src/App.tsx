import { BrowserRouter, Routes, Route } from 'react-router-dom';

function Home() {
  return <div className="p-6 text-2xl font-bold">WELO Storefront</div>;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<div className="p-6">Login Page</div>} />
      </Routes>
    </BrowserRouter>
  );
}