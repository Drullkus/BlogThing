import React, {useState} from 'react';
import Cookies from 'js-cookie';
import {BrowserRouter as Router, Link, Route, Routes} from "react-router-dom";

const ReactDOM = require('react-dom');
const Axios = require('axios');


class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            authorized: !!Cookies.get('session')
        };
        this.session = this.session.bind(this);
    }

    componentDidMount() {

    }

    session(valid) {
        this.setState({authorized: valid});
    }

    render() {
        if (this.state.authorized) {
            return (<Profile session={this.session}/>);
        } else {
            return (
                <Router>
                    <Routes>
                        <Route path="/" element={<Login session={this.session}/>}/>
                        <Route path="/register" element={<Register session={this.session}/>}/>
                    </Routes>
                </Router>
            );
        }
    }
}

function Profile(props) {

    function logout() {
        Axios.post('/api/user/logout')
            .then(() => props.session(false))
            .catch(() => props.session(false))
    }

    return (
        <div className="main">
            <div className="section">
                <button className="button" onClick={logout}>Logout</button>
            </div>
            <div className="section">
                <h1>Profile</h1>
            </div>
        </div>
    )

}

function Login(props) {

    const [result, setResult] = useState({});

    function handleSubmit(event) {
        event.preventDefault();
        let {email, password} = document.forms[0];
        Axios.post('/api/user/login', {
            'email': email.value,
            'password': password.value
        }).then(() => props.session(true))
            .catch(error => setResult(error.response.data));
    }

    function oAuth() {
        window.open("https://github.com/login/oauth/authorize?scope=user&client_id=16f5cd1d403c499d26a4&redirect_uri=http://localhost:8080/oauth/github", "_self")
    }

    return (
        <div className="main">
            <div className="section">
                <Link to="/register">Register</Link>
            </div>
            <div className="section">
                <h1>{result.error}</h1>
            </div>
            <form onSubmit={handleSubmit}>
                <div className="section">
                    <label>Email</label>
                    <input className="entry" type="text" name="email" required/>
                </div>
                <div className="section">
                    <label>Password</label>
                    <input className="entry" type="password" name="password" required/>
                </div>
                <div className="section">
                    <p>
                        <input className="button" type="submit" value="Login"/>
                    </p>
                </div>
            </form>
            <div className="section">
                <p>
                    <button className="button" onClick={oAuth}>Login With GitHub</button>
                </p>
            </div>
        </div>
    );

}

function Register(props) {

    const [result, setResult] = useState({});

    function handleSubmit(event) {
        event.preventDefault();
        let {email, name, password} = document.forms[0];
        Axios.post('/api/user/register', {
            'email': email.value,
            'name': name.value,
            'password': password.value
        }).then(() => props.session(true))
            .catch(error => setResult(error.response.data));
    }

    return (
        <div className="main">
            <div className="section">
                <Link to="/">Login</Link>
            </div>
            <div className="section">
                <h1>{result.error}</h1>
            </div>
            <form onSubmit={handleSubmit}>
                <div className="section">
                    <label>Email</label>
                    <input className="entry" type="text" name="email" required/>
                </div>
                <div className="section">
                    <label>Display Name</label>
                    <input className="entry" type="text" name="name" required/>
                </div>
                <div className="section">
                    <label>Password</label>
                    <input className="entry" type="password" name="password" required/>
                </div>
                <div className="section">
                    <p>
                        <input className="button" type="submit" value="Register"/>
                    </p>
                </div>
            </form>
        </div>
    );

}

ReactDOM.render(<App/>, document.getElementById('react'))