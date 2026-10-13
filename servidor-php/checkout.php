<?php
require __DIR__.'/config.php';
if($_SERVER['REQUEST_METHOD']!=='POST')respond(['error'=>'Método inválido'],405);
$device=device();$order=bin2hex(random_bytes(16));
$db=db();$db->prepare('INSERT INTO orders (id,device,created) VALUES (?,?,?)')->execute([$order,$device,time()]);
$payload=['handle'=>INFINITE_HANDLE,'order_nsu'=>$order,'webhook_url'=>BASE_URL.'/webhook.php',
 'redirect_url'=>BASE_URL.'/retorno.php','items'=>[['quantity'=>1,'price'=>ANNUAL_CENTS,'description'=>'Fortivum acesso anual 365 dias']]];
$ch=curl_init('https://api.checkout.infinitepay.io/links');
curl_setopt_array($ch,[CURLOPT_POST=>true,CURLOPT_POSTFIELDS=>json_encode($payload),CURLOPT_HTTPHEADER=>['Content-Type: application/json'],CURLOPT_RETURNTRANSFER=>true,CURLOPT_TIMEOUT=>15]);
$raw=curl_exec($ch);$code=curl_getinfo($ch,CURLINFO_HTTP_CODE);curl_close($ch);
$r=json_decode($raw?:'',true);$url=$r['url']??'';
if($code<200||$code>=300||!is_string($url)||!str_starts_with($url,'https://checkout.infinitepay.com.br/'))respond(['error'=>'Não foi possível criar cobrança. Habilite Checkout Integrado na InfinitePay e revise sua InfiniteTag.'],502);
respond(['url'=>$url]);
