const Message = () => {
    const mes = [
        {
            Author: 'Michal',
            message_text: 'siema',
            Cdata: '18.07.2022',
        }
    ]

    const mes_list = mes.map((a) =>
        <p style={{ border: 'solid gray', borderRadius: '15px', backgroundColor: '#F9F9F9', padding: '0px', width: "fit-content", height: "fit-content"}}>
            <span style={{ margin: "10px" }}>
                <span style={{ color: 'black', fontSize: '15px' }}>
                    {a.Author}
                </span>
                <br></br>
                <span style={{ fontSize: '30px', padding: '0px' }}>
                    {a.message_text}
                </span>
                <br></br>
                <span style={{ color: 'black', fontSize: '15px', padding: '0px' }}>
                    {a.Cdata}
                </span>
            </span>
        </p>)

    return (
        <h1>{mes_list}</h1>
    )
}
export default Message