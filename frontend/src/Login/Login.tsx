import React, { useState } from 'react';
import PropTypes from 'prop-types';

async function loginUser(verification: any) {
    return fetch('http://localhost:3000/Login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(verification)
    })
        .then(data => data.json())
}

export default function Login ({ setToken }: {setToken: any}) {
    const [username, setUserName] = useState();

    const handleSubmit = async (e: any) => {
        e.preventDefault();
        const token = await loginUser({
            username
        });
        setToken(token);
    }

    return (
        <div className="login-wrapper">
            <h1>Zaloguj się</h1>
            <form onSubmit={handleSubmit}>
                <label>
                    <input type="text" placeholder='Login' onChange={(e: any) => setUserName(e.target.value)} />
                </label>
                <label>
                    <input type="password" placeholder='Password' disabled />
                </label>
                <div>
                    <button type="submit">Submit</button>
                </div>
            </form>
        </div>
    )
}

Login.propTypes = {
    setToken: PropTypes.func.isRequired
};