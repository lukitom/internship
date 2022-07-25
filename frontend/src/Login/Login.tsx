import React, { useState } from 'react';
import PropTypes from 'prop-types';
import axios from 'axios';
const BASE_URL: string = process.env.REACT_APP_SERVER_HOST || 'http://localhost:8080';

async function loginUser(verification: any) {
    axios.post(BASE_URL + '/login', {
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(verification)
    })
        .then((data) => {
            console.log(data);
        }, (error) => {
            alert(error)
        })
};

export default function Login({ setToken }: { setToken: any }) {
    const [username, setUserName] = useState();

    const handleSubmit = async (e: any) => {
        e.preventDefault();
        const token = await loginUser({
            nickname: username
        }); console.log(token)
        setToken(token);
    }

    return (
        <div className='login-wrapper' style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexDirection: 'column'
        }}>
            <h1>Zaloguj się</h1>
            <form onSubmit={handleSubmit}>
                <label>
                    <input style={{
                        border: 'solid 1',
                        borderRadius: '10px',
                        borderColor: 'green',
                        padding: '5px',
                        margin: '5px'
                    }}

                        type='text' placeholder='Login' onChange={(e: any) => setUserName(e.target.value)} />
                </label><br />
                <label>
                    <input style={{
                        border: 'solid 1',
                        borderRadius: '10px',
                        borderColor: 'gray',
                        padding: '5px',
                        margin: '5px'
                    }}

                        type="password" placeholder='Password' disabled />
                </label><br />

                <button style={{
                    display: 'center',
                    border: 'solid 1',
                    borderRadius: '10px'
                }}
                    type="submit">Submit</button>
            </form>
        </div>
    )
}

Login.propTypes = {
    setToken: PropTypes.func.isRequired
};