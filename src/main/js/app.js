import React, {useState} from 'react';
import {BrowserRouter as Router, Link, Route, Routes} from "react-router-dom";
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
        .then(() => window.location.href = "/login")
        .catch(() => window.location.href = "/login")
}

class Home extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            self: {},
            posts: [],
            userMap: {}
        };
        this.profile = this.profile.bind(this);
        this.refresh = this.refresh.bind(this);
    }

    componentDidMount() {
        Axios.get('/api/users/self').
        then(response => this.setState({
            self: response.data,
            posts: this.state.posts,
            userMap: this.state.userMap
        })).
        catch(() => window.location.href = "/login");
        this.refresh();
    }

    refresh() {
        Axios.post('/api/posts').then(response => {
            let postData = [];
            let map = {};
            let cache = [];
            response.data.data && response.data.data.forEach(post => {
                let authorId = post.author;
                if (!this.state.userMap[authorId] && !cache.includes(authorId)) {
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

    profile() {
        this.state.self.id && (window.location.href = "/profile/" + this.state.self.id);
    }

    render() {
        let posts = [];
        this.state.posts.forEach(p => posts.push(<Post key={p.id} post={p} users={this.state.userMap} self={this.state.self} refresh={this.refresh}/>))
        return (
            <div className="main">
                <div className="section">
                    <button className="button" onClick={logout}>Logout</button>
                </div>
                <div className="center">
                    <button className="button" onClick={this.profile}>My Profile</button>
                </div>
                <div className="section">
                    <h1>Home</h1>
                </div>
                <Poster refresh={this.refresh}/>
                {posts}
            </div>
        )
    }
}

class Post extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            edit: false
        };
        this.toggleEdit = this.toggleEdit.bind(this);
        this.submitEdit = this.submitEdit.bind(this);
        this.delete = this.delete.bind(this);
    }

    toggleEdit(event) {
        event.preventDefault();
        this.setState({
            edit: !this.state.edit
        })
    }

    submitEdit(event) {
        event.preventDefault();
        let {text} = document.forms["post_" + this.props.post.id];
        Axios.post('/api/post/edit', {
            'data': text.value,
            'id': this.props.post.id
        }).then(() => {
            this.props.refresh();
        });
        this.setState({
            edit: false
        })
    }

    delete(event) {
        event.preventDefault();
        Axios.post('/api/post/delete/' + this.props.post.id).then(() => this.props.refresh());
    }

    componentDidMount() {

    }

    render() {
        if (!this.state.edit)
            return (
                <div className="post">
                    <PostDisplay post={this.props.post} users={this.props.users}/>
                    <p/>
                    <div className="options">
                        <a className="comments" href="#">Comments</a>
                        {(this.props.self && this.props.self.id === this.props.post.author) &&
                        <a className="edit" href="#" onClick={this.toggleEdit}>Edit</a>}
                        {(this.props.self && (this.props.self.admin || this.props.self.id === this.props.post.author)) &&
                        <a className="delete" href="#" onClick={this.delete}>Delete</a>}
                    </div>
                </div>
            )
        else
            return (
                <div className="post">
                    <form name={"post_" + this.props.post.id} onSubmit={this.submitEdit}>
                        <textarea name="text" rows="10" cols="157" defaultValue={this.props.post.text}/>
                        <p/>
                        <input className="button" type="submit" value="Finish"/>
                    </form>
                    <div className="options">
                        <a className="edit" href="#" onClick={this.toggleEdit}>Cancel</a>
                    </div>
                </div>
            )
    }

}

function PostDisplay(props) {

    function timeSince(timestamp) {
        let date = new Date(timestamp * 1000);
        let seconds = Math.floor((new Date() - date) / 1000);
        let interval = seconds / 31536000;
        if (interval > 1)
            return Math.floor(interval) + " years";
        interval = seconds / 2592000;
        if (interval > 1)
            return Math.floor(interval) + " months";
        interval = seconds / 86400;
        if (interval > 1)
            return Math.floor(interval) + " days";
        interval = seconds / 3600;
        if (interval > 1)
            return Math.floor(interval) + " hours";
        interval = seconds / 60;
        if (interval > 1)
            return Math.floor(interval) + " minutes";
        return Math.floor(seconds) + " seconds";
    }

    return (
        <div>
            <strong><Link to={"/profile/" + props.post.author}>{props.users[props.post.author] && props.users[props.post.author].name}</Link></strong>
            <span className="timestamp"><u> {timeSince(props.post.timestamp)} ago</u></span>
            <br/>
            <pre>{props.post.text}</pre>
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
        this.refresh = this.refresh.bind(this);
    }

    componentDidMount() {
        Axios.get('/api/users/' + this.state.id).then(response => {
            this.setState({
                id: this.state.id,
                user: response.data,
                posts: this.state.posts
            });
        });
        this.refresh();
    }

    refresh() {
        Axios.post('/api/posts', {
            "author": Number(this.state.id)
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

    home() {
        window.location.href = "/";
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
                <div className="center">
                    <button className="button" onClick={this.home}>Home</button>
                </div>
                <div className="section">
                    <h1>{this.state.user && this.state.user.name}'s Profile</h1>
                </div>
                <Poster refresh={this.refresh}/>
                {posts}
            </div>
        )
    }

}

function Poster(props) {

    const [result, setResult] = useState({});

    function handleSubmit(event) {
        event.preventDefault();
        let {text} = document.forms[0];
        Axios.post('/api/post/submit', {
            'data': text.value
        }).then(() => {
            setResult({});
            props.refresh();
        }).catch(error => setResult(error.response.data));
        text.value = "";
    }

    return (
        <div className="section">
            <div className="section">
                <h1>{result.error}</h1>
            </div>
            <form onSubmit={handleSubmit}>
                <div className="section">
                <textarea name="text" rows="10" cols="157" placeholder="Write something..."/>
                </div>
                <div className="center">
                    <p />
                    <input className="button" type="submit" value="Post"/>
                </div>
            </form>
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
        }).then(() => window.location.href = "/")
            .catch(error => setResult(error.response.data));
    }

    function oAuth() {
        window.open("https://github.com/login/oauth/authorize?scope=user&client_id=16f5cd1d403c499d26a4&redirect_uri=http://localhost:8080/oauth/github", "_self")
    }

    return (
        <div className="center">
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
                    <p />
                    <input className="button" type="submit" value="Login"/>
                </div>
            </form>
            <div className="section">
                <p />
                <button className="button" onClick={oAuth}>Login With GitHub</button>
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
        }).then(() => window.location.href = "/")
            .catch(error => setResult(error.response.data));
    }

    return (
        <div className="center">
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
                    <p />
                    <input className="button" type="submit" value="Register"/>
                </div>
            </form>
        </div>
    );

}

ReactDOM.render(<App/>, document.getElementById('react'))