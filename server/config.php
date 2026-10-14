<?php
const HANDLE = 'pablo-henrique-532';
const PRICE_CENTS = 990;
const API_ROOT = 'https://loja-com.com/autofiscal-api';
const STORE = __DIR__ . '/private/orders.json';
function reply($data,$code=200){http_response_code($code);header('Content-Type: application/json; charset=utf-8');header('Cache-Control: no-store');echo json_encode($data);exit;}
function input(){if($_SERVER['REQUEST_METHOD']!=='POST')reply(['ok'=>false,'error'=>'Método inválido'],405);$d=json_decode(file_get_contents('php://input'),true);if(!is_array($d))reply(['ok'=>false,'error'=>'JSON inválido'],400);return $d;}
function valid_device($d){$id=$d['device_id']??'';if(!is_string($id)||!preg_match('/^[0-9a-f-]{36}$/i',$id))reply(['ok'=>false,'error'=>'Identificador inválido'],400);return $id;}
function locked($callback){$dir=dirname(STORE);if(!is_dir($dir))mkdir($dir,0700,true);$f=fopen(STORE,'c+');if(!$f)reply(['ok'=>false,'error'=>'Armazenamento indisponível'],500);flock($f,LOCK_EX);$raw=stream_get_contents($f);$data=json_decode($raw,true);if(!is_array($data))$data=[];$result=$callback($data);rewind($f);ftruncate($f,0);fwrite($f,json_encode($data));fflush($f);flock($f,LOCK_UN);fclose($f);return $result;}
function post_ip($path,$body){$c=curl_init('https://api.checkout.infinitepay.io/'.$path);curl_setopt_array($c,[CURLOPT_POST=>true,CURLOPT_POSTFIELDS=>json_encode($body),CURLOPT_HTTPHEADER=>['Content-Type: application/json'],CURLOPT_RETURNTRANSFER=>true,CURLOPT_TIMEOUT=>20,CURLOPT_SSL_VERIFYPEER=>true]);$raw=curl_exec($c);$code=curl_getinfo($c,CURLINFO_HTTP_CODE);curl_close($c);$r=json_decode($raw?:'',true);return [$code,is_array($r)?$r:[]];}
function verify_order($order){if(empty($order['transaction_nsu'])||empty($order['slug']))return false;[$code,$r]=post_ip('payment_check',['handle'=>HANDLE,'order_nsu'=>$order['order_nsu'],'transaction_nsu'=>$order['transaction_nsu'],'slug'=>$order['slug']]);return $code>=200&&$code<300&&($r['success']??false)===true&&($r['paid']??false)===true&&(int)($r['amount']??0)===PRICE_CENTS&&(int)($r['paid_amount']??0)>=PRICE_CENTS;}
