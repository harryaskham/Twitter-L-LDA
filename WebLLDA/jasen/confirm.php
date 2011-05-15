<?php
include 'EpiCurl.php';
include 'EpiOAuth.php';
include 'EpiTwitter.php';
include 'secret.php';

$twitterObj = new EpiTwitter($consumer_key, $consumer_secret);

$twitterObj->setToken($_GET['oauth_token']);
$token = $twitterObj->getAccessToken();
$twitterObj->setToken($token->oauth_token, $token->oauth_token_secret);
$twitterInfo= $twitterObj->get_accountVerify_credentials();
$twitterInfo->response;
echo "Your twitter username is {$twitterInfo->screen_name} and your profile picture is <img src=\"{$twitterInfo->profile_image_url}\">";
$tok = file_put_contents('tok', $token->oauth_token);
$sec = file_put_contents('sec', $token->oauth_token_secret);
?>

