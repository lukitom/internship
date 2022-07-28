import { useState } from 'react';
import PropTypes from 'prop-types';
import axios from "axios";
const BACKEND_BASE_URL: string = process.env.REACT_APP_SERVER_HOST || 'http://localhost:8080';

const mes = [
    {
        id: 1,
        authorNick: 'Krystian',
        content: 'Cześć.',
        createdAt: '20.07.2022',
    },
    {
        id: 2,
        authorNick: 'Kuba',
        content: 'Hej.',
        createdAt: '20.07.2022',
    },
    {
        id: 3,
        authorNick: 'Krystian',
        content: 'Co robisz?',
        createdAt: '20.07.2022',
    },
    {
        id: 4,
        authorNick: 'Kuba',
        content: 'Piszę kod w React.',
        createdAt: '20.07.2022',
    },
    {
        id: 5,
        authorNick: 'Kuba',
        content: 'A ty co robisz?',
        createdAt: '20.07.2022',
    },
    {
        id: 6,
        authorNick: 'Kuba',
        content: 'Gram w gry.',
        createdAt: '20.07.2022',
    },
    {
        id: 7,
        authorNick: 'Krystian',
        content: 'W co grasz?',
        createdAt: '20.07.2022',
    },
    {
        id: 8,
        authorNick: 'Kuba',
        content: 'Terrarie',
        createdAt: '20.07.2022',
    }
]

async function messagePost(verification: any) {
    return await axios({
        method: 'post',
        url: BACKEND_BASE_URL + '/messages',
        data: verification
    })
        .then(res => res.data);
};

export default function Message({ setToken }: { setToken: any }) {
    const [Message, setMessage] = useState();

    const handleSubmit = async (e: any) => {
        e.preventDefault();
        const token = await messagePost({
            content: Message
        });
        setToken(token);
    }

    return (
        <>
            {mes.map(({ authorNick, content }) => (
                <span>
                    {authorNick}
                    <p
                        style={{
                            border: "solid gray",
                            borderRadius: "15px",
                            backgroundColor: "#F9F9F9",
                            padding: "10px",
                            width: "fit-content",
                            height: "fit-content",
                            fontSize: "15px",
                            color: "black",
                            margin: "10px 0px 0px 10px",
                        }}
                    >
                        {content}
                    </p>
                </span>
            ))}
            <span style={{ bottom: '0' }}>
                <form onSubmit={handleSubmit}>
                    <input type='text' placeholder='Message' onChange={(e: any) => setMessage(e.target.value)} />
                    <button type='submit'>Wyśli</button>
                </form>
            </span>
        </>
    );
}

Message.propTypes = {
    setToken: PropTypes.func.isRequired
};
