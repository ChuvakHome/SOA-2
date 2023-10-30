<?php
    define("CURL_ESTABLISH_ERROR_CODE", 7);
    define("BAD_REQUEST_HTTP_CODE", 400);
    define("BAD_GATEWAY_HTTP_CODE", 502);

    function is_valid_request($json_request) {
        return isset($json_request['url']) && isset($json_request['service']);
    }

    $curl = '/usr/local/bin/curl';
    $json = file_get_contents('php://input');
    $body = json_decode($json, true);

    if (!is_valid_request($body)) {
        http_response_code(constant("BAD_REQUEST_HTTP_CODE"));

        exit();
    }

    $request_method = isset($body['method']) ? $body['method'] : 'GET';
    $request_url = $body['url'];
    $request_params = isset($body['url-params']) ?: '';

    if (isset($body['url-params'])) {
        $url_params = $body['url-params'];
        $params_temp = array();

        foreach ($url_params as $param) {
            $param_name = $param[0];
            $param_value = $param[1];
            array_push($params_temp, "$param_name=$param_value");
        }

        $request_params = implode('&', $params_temp);
    }

    $request_body = isset($body['body']) && $request_method !== 'GET' && $request_method !== 'HEAD' ? $body['body'] : '{}';

    $cerl_path = null;

    if ($body['service'] === 1) {
        $service_url = 'https://localhost:10121';
        $cert_path = '/home/studs/s311683/soa/lab2/preparing/certs/client_trust.pem';
    }
    else if ($body['service'] === 2) {
        $service_url = 'https://localhost:9443';
        $cert_path = '/home/studs/s311683/soa/lab2/preparing/certs/server_trust.pem';
    }

    exec("$curl -s -w '\n%{http_code}' -X '$request_method' '$service_url/$request_url?$request_params' -H 'Content-Type: application/json' -d '$request_body' --cacert $cert_path", $output, $retval);

    if ($retval === constant("CURL_ESTABLISH_ERROR_CODE")) {
        http_response_code(constant("BAD_GATEWAY_HTTP_CODE"));
    }
    else {
        $response_body = $output[0];
        $response_code = $output[1];
        
        $response = array('response_code' => $response_code, 'response_body' => $response_body);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode($response);
    }
?>
