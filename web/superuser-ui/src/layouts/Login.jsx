// @ts-nocheck

import { useNavigate, useLocation, useParams } from 'react-router-dom';

function Login({ component: Component, ...rest }) {
    const navigate = useNavigate();
    const location = useLocation();
    const params = useParams();

    return (
        <Component {...rest} navigate={navigate} location={location} params={params} />
    );
}

export default Login;