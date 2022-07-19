const mes = [
    {
        Author: 'Michal',
        message1: 'Hej',
        message2: 'Co porabiasz?',
        message3: 'Co robisz?',
        message4: 'Jakie danie dzisiaj jadłeś?',
        message5: 'Chcesz pograć?',
        message6: 'Wyskoczysz z nami do parku?',
        message7: 'Ide do kina',
        message8: 'Jestem w sklepie',
        Cdata: '20.07.2022',
    }
]

let Author, message1, message2, message3, message4, message5, message6, message7, message8, Cdata;
[Author, message1, message2, message3, message4, message5, message6, message7, message8, Cdata] = 
['Michał', 'Hej', 'Co porabiasz?', 'Co robisz?', 'Jakie danie dzisiaj jadłeś?', 'Chcesz pograć?', 'Wyskoczysz z nami do parku?', 'Ide do kina', 'Jestem w sklepie', '20.07.2022', ];

const Message = () => {   

    const mes_list = mes.map((messages) =>
        <p style={{ border: 'solid gray', borderRadius: '15px', backgroundColor: '#F9F9F9', padding: '0px', width: "fit-content", height: "fit-content"}}>
            <span style={{ margin: '10px' }}>

                <span style={{ color: 'black', fontSize: '15px' }}>
                    {messages.Author}
                </span>
                <br></br>

                <span style={{ fontSize: '30px', padding: '0px' }}>
                    {messages.message1}
                </span>
                <br></br>

                <span style={{ fontSize: '30px', padding: '0px' }}>
                    {messages.message2}
                </span>
                <br></br>

                <span style={{ fontSize: '30px', padding: '0px' }}>
                    {messages.message3}
                </span>
                <br></br>

                <span style={{ fontSize: '30px', padding: '0px' }}>
                    {messages.message4}
                </span>
                <br></br>

                <span style={{ fontSize: '30px', padding: '0px' }}>
                    {messages.message5}
                </span>
                <br></br>

                <span style={{ fontSize: '30px', padding: '0px' }}>
                    {messages.message6}
                </span>
                <br></br>

                <span style={{ fontSize: '30px', padding: '0px' }}>
                    {messages.message7}
                </span>
                <br></br>

                <span style={{ fontSize: '30px', padding: '0px' }}>
                    {messages.message8}
                </span>
                <br></br>

                <span style={{ color: 'black', fontSize: '15px', padding: '0px' }}>
                    {messages.Cdata}
                </span>
            </span>
        </p>)

    return (
        <h1>{mes_list}</h1>
    )
}
export default Message