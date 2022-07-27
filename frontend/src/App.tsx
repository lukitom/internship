import { useState } from 'react';
import { Route, Routes } from 'react-router-dom';

import Login from './Login/Login';
import Preferences from './components/Preferences/Preferences';

function App() {
  const [token, setToken] = useState();

  if (!token) {
    return <Login setToken={setToken} />
  }

  return (
    <div className="wrapper">
      <Routes>
        <Route path="/" element={<Preferences />} />
      </Routes>
    </div>
  );
}

export default App;
