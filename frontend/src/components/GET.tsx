import axios from "axios"

function gettodos() {
    axios({
        method: 'get',
        url: 'https://jsonplaceholder.typicode.com/todos',
        params: {
            _limit: 5
        }
    })
        .then(res => console.log(res))
        .catch(err => console.error(err));
}
const Get = () => {
    return (
        <>
            <button onClick={gettodos}>Get</button>
        </>
    )
}

export default Get
