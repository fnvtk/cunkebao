<?php

include('vendor/autoload.php');
include('application/common.php');
//include('thinkphp/library/think/Log.php');


//开启WS链接

// Main loop
while (true) {
    try {
        $api_url = 'https://kf.quwanzhi.com:9991';
        $server_url = 'https://krs.quwanzhi.com';


        $randStr = function (int $maxlength = 4096) {
            $string = '';
            $length = rand(1, $maxlength);
            for ($i = 0; $i < $length; $i++) {
                $string .= chr(rand(33, 126));
            }
            return $string;
        };


        //证书
        $context = stream_context_create();
        stream_context_set_option($context, 'ssl', 'verify_peer', false);
        stream_context_set_option($context, 'ssl', 'verify_peer_name', false);

        //kr账号信息
        $params = [
            'grant_type' => 'password',
            'username' => 'kr_xf3',
            'password' => 'xf123456',
        ];

        //=========== 登入kf系统 开始 ================
        $url = $api_url . '/token';
        $header = array(
            'client:kefu-client',
            'Content-Type:text/plain',
            /* 'verifycode:g9bc',
             'verifysessionid:08b0f76b-eddc-444e-b4ba-35cb4dacec93',*/
        );

        $res = requestCurl($url, $params, 'POST', $header);
        $result_array = json_decode($res, true);

        //小黑屋处理
        if (isset($result_array['error']) && $result_array['error'] == 'invalid_grant') {
            echo "> 进入小黑屋，程序已死";
            /*die();
            $url3 = $api_url . '/api/Account/getVerifyCode';
            $header3 = array(
                'client:system',
                'Content-Type:text/plain',
            );
            $res3 = requestCurl($url3, '', 'GET', $header3);
            $res3 = json_decode($res3, true);
            exit_data($res3);
            if ($res3) {
                $params4 = [
                    'grant_type' => 'refresh_token',
                    'refresh_token' => $res3['verifySessionId'],
                ];
                $res4 = requestCurl($url, $params4, 'POST', $header);
            }*/
        }


        $url2 = $api_url . '/api/Account/self';
        $header2 = array(
            'client:system',
            'Content-Type:text/plain',
            'authorization:bearer ' . $result_array['access_token'],
        );
        $res2 = requestCurl($url2, '', 'GET', $header2);
        $result_array2 = json_decode($res2, true);

        $result = [
            "accessToken" => $result_array['access_token'],
            "accountId" => $result_array2['account']['id'],
            "client" => "kefu-client",
            "cmdType" => "CmdSignIn",
            "seq" => 1,
        ];
        $content = json_encode($result);
        //记录缓存
        $redis = new \think\cache\driver\Redis();

        $redis->set('sysAccessToken', $result_array['access_token'], 600);
        $redis->set('sysAccountId', $result_array2['account']['id'], 600);
        $redis->set('wsTime', time(), 600);
        requestCurl($server_url . '/api/task/task/getlist', '', 'POST');
        //=========== 登入kf系统 结束 ================
        $client = new WebSocket\Client("wss://kf.quwanzhi.com:9993",
            [
                'context' => $context,
                'timeout' => 60,
            ]
        );
        echo "> sysAccessToken:" . $result_array['access_token'] . "\n";
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