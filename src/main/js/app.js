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
                    <Route path="/post/:id" element={<CommentSectionWrapper />}/>
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
        document.title = "Home";
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
        this.state.posts.forEach(p => posts.push(<Post key={p.id} type="post" post={p} users={this.state.userMap} self={this.state.self} refresh={this.refresh}/>))
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

function ProfileWrapper(props) {
    const { id } = useParams();
    return (<Profile id={id}/>)
}

class Profile extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            self: {},
            user: {},
            posts: []
        };
        this.refresh = this.refresh.bind(this);
    }

    componentDidMount() {
        document.title = "Profile";
        Axios.get('/api/users/self').
        then(response => this.setState({
            self: response.data,
            user: this.state.user,
            posts: this.state.posts
        }))
        Axios.get('/api/users/' + this.props.id).then(response => {
            this.setState({
                user: response.data,
                posts: this.state.posts
            });
        });
        this.refresh();
    }

    refresh() {
        Axios.post('/api/posts', {
            "author": Number(this.props.id)
        }).then(response => {
            let postData = [];
            response.data.data && response.data.data.forEach(post => postData.push(post));
            this.setState({
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
        userData[this.props.id] = this.state.user;
        this.state.posts.forEach(p => posts.push(<Post key={p.id} type="post" post={p} users={userData} self={this.state.user} />))
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
                {this.state.self.id === this.state.user.id && <Poster refresh={this.refresh}/>}
                {posts}
            </div>
        )
    }

}

function CommentSectionWrapper(props) {
    const { id } = useParams();
    return (<CommentSection id={id}/>)
}

class CommentSection extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            comments: [],
            post: {},
            self: {},
            userMap: {}
        };
        this.refresh = this.refresh.bind(this);
    }

    componentDidMount() {
        document.title = "Comments";
        Axios.get('/api/users/self').
        then(response => {
            this.setState({
                comments: this.state.comments,
                post: this.state.posts,
                self: response.data,
                userMap: this.state.userMap
            });
            Axios.get('/api/posts/' + Number(this.props.id)).
            then(response => {
                this.setState({
                    comments: this.state.comments,
                    post: response.data,
                    self: this.state.self,
                    userMap: this.state.userMap
                });
                this.refresh();
            }).
            catch(() => window.location.href = "/");
        }).
        catch(() => window.location.href = "/login");
    }

    refresh() {
        Axios.post('/api/comments', {
            'post': Number(this.props.id)
        }).then(response => {
            let commentData = [];
            let map = {};
            let cache = [];
            this.state.post && Axios.get('/api/users/' + this.state.post.author).then(res => {
                map[this.state.post.author] = res.data;
                this.setState({
                    comments: this.state.comments,
                    post: this.state.post,
                    self: this.state.self,
                    userMap: map
                });
                response.data.data && response.data.data.forEach(comment => {
                    let authorId = comment.author;
                    if (!this.state.userMap[authorId] && !cache.includes(authorId)) {
                        cache.push(authorId);
                        Axios.get('/api/users/' + authorId).then(r => {
                            let user = r.data;
                            map[user.id] = user;
                            this.setState({
                                comments: this.state.comments,
                                post: this.state.post,
                                self: this.state.self,
                                userMap: map
                            });
                        })
                    }
                    commentData.push(comment);
                });
                this.setState({
                    comments: commentData,
                    post: this.state.post,
                    self: this.state.self,
                    userMap: this.state.userMap
                });
            });
        });
    }

    home() {
        window.location.href = "/";
    }

    render() {
        let comments = [];
        this.state.comments.forEach(p => comments.push(<Post key={p.id} type="comment" post={p} users={this.state.userMap} self={this.state.self} refresh={this.refresh}/>))
        return (
            <div className="main">
                <div className="section">
                    <button className="button" onClick={logout}>Logout</button>
                </div>
                <div className="center">
                    <button className="button" onClick={this.home}>Home</button>
                </div>
                <div className="section">
                    <h1>Comments</h1>
                </div>
                {this.state.post && this.state.self && <Post key="parent" parent={true} type="post" post={this.state.post} users={this.state.userMap} self={this.state.self} refresh={this.refresh} />}
                <Poster parent={this.props.id} refresh={this.refresh}/>
                {comments}
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
        let {text} = document.forms["post_" + this.props.type + "_" + this.props.post.id];
        Axios.post('/api/' + this.props.type + '/edit', {
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
        Axios.get('/api/' + this.props.type + '/delete/' + this.props.post.id).then(() => this.props.refresh());
    }

    componentDidMount() {

    }

    render() {
        if (!this.state.edit)
            return (
                <div className="post">
                    <strong><Link to={"/profile/" + this.props.post.author}>{this.props.users[this.props.post.author] && this.props.users[this.props.post.author].name}</Link></strong>
                    <span className="timestamp"><u>{timeSince(this.props.post.timestamp)} ago</u></span>
                    <br/>
                    <pre>{this.props.post.text}</pre>
                    <p/>
                    <div className="options">
                        {(this.props.type === "post" && !this.props.parent) && <Link to={"/post/" + this.props.post.id}>Comments</Link>}
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
                    <form name={"post_" + this.props.type + "_" + this.props.post.id} onSubmit={this.submitEdit}>
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

function Poster(props) {

    const [result, setResult] = useState({});

    function handleSubmit(event) {
        event.preventDefault();
        let {text} = document.forms[0];
        if (props.parent) {
            Axios.post('/api/comment/submit', {
                'post': Number(props.parent),
                'data': text.value
            }).then(() => {
                setResult({});
                props.refresh();
            }).catch(error => setResult(error.response.data));
        } else {
            Axios.post('/api/post/submit', {
                'data': text.value
            }).then(() => {
                setResult({});
                props.refresh();
            }).catch(error => setResult(error.response.data));
        }
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
                    <input className="button" type="submit" value={props.parent ? "Comment" : "Post"}/>
                </div>
            </form>
        </div>
    )

}

function Login(props) {

    document.title = "Login";

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

    document.title = "Register";

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