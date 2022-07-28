import { useState } from 'react';
import { Route, Routes } from 'react-router-dom';
import Login from './Login/Login';
import Message from './Message/Message';

function App() {
  const [token, setToken] = useState();

  if (!token) {
    return <Login setToken={setToken} />
  }

  return (
    <div>
      <Routes>
        <Route path="/" element={<Message setToken={token}/>} />
        {/* <Route path="/" component={props: any => <Message {...props} />} />; */}
      </Routes>
    </div>
  );
}

export default App;

