const React = require("react");
const ReactDOM = require('react-dom');
const Axios = require('axios');

class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            result: {}
        };
        this.handleSubmit = this.handleSubmit.bind(this);
        this.oAuth = this.oAuth.bind(this);
    }

    componentDidMount() {

    }

    handleSubmit(event) {
        event.preventDefault();
        let { email, password } = document.forms[0];
        Axios.post('/api/user/login', {
            'email': email.value,
            'password': password.value
        }).then(response => {
            this.setState({result: response.data});
        }).catch(error => {
            this.setState({result: error.response.data});
        });
    }

    oAuth() {
        window.open("https://github.com/login/oauth/authorize?scope=user&client_id=16f5cd1d403c499d26a4&redirect_uri=http://localhost:8080/oauth/github","_self")
    }

    render() {
        return (
            <div className="login">
                <div className="section">
                    <h1>{this.state.result.error}</h1>
                </div>
                <div className="section">
                    <h1>{this.state.result.success}</h1>
                </div>
                <form onSubmit={this.handleSubmit}>
                    <div className="section">
                        <label>Email</label>
                        <input className="entry" type="text" name="email" required />
                    </div>
                    <div className="section">
                        <label>Password</label>
                        <input className="entry" type="password" name="password" required />
                    </div>
                        <div className="section">
                            <p>
                                <input className="button" type="submit" />
                            </p>
                        </div>
                </form>
                    <div className="section">
                        <p>
                            <button className="button" onClick={this.oAuth} >GitHub</button>
                        </p>
                    </div>
            </div>
        );
    }
}

ReactDOM.render(<App/>, document.getElementById('react'))