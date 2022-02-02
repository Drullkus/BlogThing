const React = require('react');
const ReactDOM = require('react-dom');
const Axios = require('axios');

class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {users: []};
    }

    componentDidMount() {
        Axios.get('/api/users').then(response => {
            this.setState({users: response.data});
        });
    }

    render() {
        return (
            <UserList users={this.state.users}/>
        );
    }
}

class UserList extends React.Component{
    render() {
        const users = this.props.users.map(user =>
            <User key={user.id} user={user}/>
        );
        return (
            <table>
                <tbody>
                    <tr>
                        <th>ID</th>
                        <th>First Name</th>
                        <th>Last Name</th>
                    </tr>
                    {users}
                </tbody>
            </table>
        )
    }
}

class User extends React.Component{
    render() {
        return (
            <tr>
                <td>{this.props.user.id}</td>
                <td>{this.props.user.firstname}</td>
                <td>{this.props.user.lastname}</td>
            </tr>
        )
    }
}

ReactDOM.render(<App />, document.getElementById('react'))