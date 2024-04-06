<?php

include('vendor/autoload.php');
include('application/common.php');

$randStr = function (int $maxlength = 4096) {
    $string = '';
    $length = rand(1, $maxlength);
    for ($i = 0; $i < $length; $i++) {
        $string .= chr(rand(33, 126));
    }
    return $string;
};

//=========== 登入kf系统 开始 ================
$params = [
    'grant_type' => 'password',
    'username' => 'dygq2',
    'password' => 'dygq123',
];
$url = 'https://kf.quwanzhi.com:9991/token';
$header = array(
    'client:kefu-client',
    'Content-Type:text/plain'
);

$res = requestCurl($url,$params,'POST',$header);
$result_array = json_decode($res, 1);


$url = 'https://kf.quwanzhi.com:9991/api/Account/self';
$header = array(
    'client:system',
    'Content-Type:text/plain',
    'authorization:bearer ' . $result_array['access_token'],
);

$res = requestCurl($url,'','GET',$header);
$result_array2 = json_decode($res, 1);

//=========== 登入kf系统 结束 ================


//证书
$context = stream_context_create();
stream_context_set_option($context, 'ssl', 'verify_peer', false);
stream_context_set_option($context, 'ssl', 'verify_peer_name', false);
//开启WS链接
$seq = 1;
$result = [
    "accessToken" => $result_array['access_token'],
    "accountId" => $result_array2['account']['id'],
    "client" => "kefu-client",
    "cmdType" => "CmdSignIn",
    "seq" => $seq,
];

$content = json_encode($result);
// Main loop
while (true) {
    try {
        $client = new WebSocket\Client("wss://kf.quwanzhi.com:9993",
            [
                'filter' => ['text', 'binary', 'ping', 'pong', 'close', 'receive'],
                'context' => $context,
                'headers' => [
                    'Sec-WebSocket-Protocol' => 'soap',
                    'origin' => 'localhost',
                ],
                'timeout' => 10,
            ]
        );
        // print_r($context);
        // exit();
        echo "> 重新链接\n";
        $client->send($content);
        try {
            while (true) {
                try {
                    $client->ping();
                    $received = $client->receive();
//                    $received = json_decode($received,1);
                    echo "> Received : {$received}\n";
                } catch (Throwable $e) {
                    echo "此时没有消息\n";
                }
                sleep(1);

            }
        } catch (Throwable $e) {
            echo "ERROR I/O: {$e->getMessage()} [{$e->getCode()}]\n";
        }
    } catch (Throwable $e) {
        echo "ERROR: {$e->getMessage()} [{$e->getCode()}]\n";
    }
    sleep(rand(1, 5));
}