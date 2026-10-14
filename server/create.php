<?php
require __DIR__.'/config.php';$d=input();$device=valid_device($d);
$active=locked(function(&$rows)use($device){foreach($rows as $row)if($row['device']===$device&&($row['expires']??0)>time())return true;return false;});
if($active)reply(['ok'=>false,'error'=>'Licença já ativa']);
$order=bin2hex(random_bytes(16));$body=['handle'=>HANDLE,'order_nsu'=>$order,'redirect_url'=>API_ROOT.'/return.php','webhook_url'=>API_ROOT.'/webhook.php','items'=>[['quantity'=>1,'price'=>PRICE_CENTS,'description'=>'AutoFiscal IA - acesso anual']]];
[$code,$result]=post_ip('links',$body);$url=$result['url']??'';
if($code<200||$code>=300||!is_string($url)||strpos($url,'https://checkout.infinitepay.com.br/')!==0)reply(['ok'=>false,'error'=>'Falha ao criar cobrança na InfinitePay. Confira a ativação do Checkout Integrado.'],502);
locked(function(&$rows)use($device,$order){$rows[$order]=['device'=>$device,'order_nsu'=>$order,'created'=>time(),'expires'=>0,'slug'=>'','transaction_nsu'=>''];});reply(['ok'=>true,'url'=>$url,'order_nsu'=>$order]);
