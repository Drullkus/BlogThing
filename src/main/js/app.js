import React, {useState} from 'react';
import {
    BrowserRouter as Router,
    Link,
    Route,
    Routes
} from "react-router-dom";
import {useParams} from "react-router";

const ReactDOM = require('react-dom');
const Axios = require('axios');

class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
        };
    }

    componentDidMount() {

    }

    render() {
        return (
            <Router>
                <Routes>
                    <Route path="/" element={<Home />}/>
                    <Route path="/profile/:id" element={<ProfileWrapper />}/>
                    <Route path="/login" element={<Login />}/>
                    <Route path="/register" element={<Register />}/>
                </Routes>
            </Router>
        );
    }
}

function logout() {
    Axios.post('/api/user/logout')
        .then(() => window.location.replace("/login"))
        .catch(() => window.location.replace("/login"))
}

class Home extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            self: {},
            posts: [],
            userMap: {}
        };
    }

    componentDidMount() {
        Axios.get('/api/users/self').
        then(response => this.setState({
            self: response.data,
            posts: this.state.posts,
            userMap: this.state.userMap
        })).
        catch(() => window.location.replace("/login"));

        Axios.post('/api/posts').then(response => {
            let postData = [];
            let map = {};
            let cache = [];
            response.data.data && response.data.data.forEach(post => {
                let authorId = post.author;
                if (!cache.includes(authorId)) {
                    cache.push(authorId);
                    Axios.get('/api/users/' + authorId).then(r => {
                        let user = r.data;
                        map[user.id] = user;
                        this.setState({
                            self: this.state.self,
                            posts: this.state.posts,
                            userMap: map
                        });
                    })
                }
                postData.push(post);
            });
            this.setState({
                self: this.state.self,
                posts: postData,
                userMap: this.state.userMap
            });
        });
    }

    render() {
        let posts = [];
        this.state.posts.forEach(p => posts.push(<Post key={p.id} post={p} users={this.state.userMap}/>))
        return (
            <div className="main">
                <div className="section">
                    <button className="button" onClick={logout}>Logout</button>
                </div>
                <div className="section">
                    <h1>Home</h1>
                </div>
                {posts}
            </div>
        )
    }
}

function Post(props) {

    return (
        <div className="post">
            <strong>{props.users[props.post.author] && props.users[props.post.author].name}</strong><br/>
            {props.post.text}
        </div>
    )

}

function ProfileWrapper(props) {
    const { id } = useParams();
    return (<Profile id={id}/>)
}

class Profile extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            id: props.id,
            user: {},
            posts: []
        };
    }

    componentDidMount() {
        Axios.get('/api/users/' + this.state.id).then(response => {
            this.setState({
                id: this.state.id,
                user: response.data,
                posts: this.state.posts
            });
        });
        Axios.post('/api/posts', {
            "author": this.state.id
        }).then(response => {
            let postData = [];
            response.data.data && response.data.data.forEach(post => postData.push(post));
            this.setState({
                id: this.state.id,
                user: this.state.user,
                posts: postData
            });
        });
    }

    render() {
        let posts = [];
        let userData = {};
        userData[this.state.id] = this.state.user;
        this.state.posts.forEach(p => posts.push(<Post key={p.id} post={p} users={userData}/>))
        return (
            <div className="main">
                <div className="section">
                    <button className="button" onClick={logout}>Logout</button>
                </div>
                <div className="section">
                    <h1>{this.state.user && this.state.user.name}'s Profile</h1>
                </div>
                {posts}
            </div>
        )
    }

}

function Login(props) {

    const [result, setResult] = useState({});

    function handleSubmit(event) {
        event.preventDefault();
        let {email, password} = document.forms[0];
        Axios.post('/api/user/login', {
            'email': email.value,
            'password': password.value
        }).then(() => window.location.replace("/"))
            .catch(error => setResult(error.response.data));
    }

    function oAuth() {
        window.open("https://github.com/login/oauth/authorize?scope=user&client_id=16f5cd1d403c499d26a4&redirect_uri=http://localhost:8080/oauth/github", "_self")
    }

    return (
        <div className="login">
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
        }).then(() => window.location.replace("/"))
            .catch(error => setResult(error.response.data));
    }

    return (
        <div className="login">
            <div className="section">
                <Link to="/login">Login</Link>
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