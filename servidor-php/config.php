<?php
// Substitua pelo seu identificador InfiniteTag, sem $. Verifique a titularidade da conta.
const INFINITE_HANDLE = 'pablo-henrique-532';
const BASE_URL = 'https://loja-com.com/fortivum-api';
const ANNUAL_CENTS = 100; // R$ 1,00; 99 para R$ 0,99
function db(): PDO {
    $dir=__DIR__.'/private'; if(!is_dir($dir)) mkdir($dir,0700,true);
    $db=new PDO('sqlite:'.$dir.'/fortivum.sqlite');
    $db->setAttribute(PDO::ATTR_ERRMODE,PDO::ERRMODE_EXCEPTION);
    $db->exec('CREATE TABLE IF NOT EXISTS licenses (device TEXT PRIMARY KEY, expires INTEGER NOT NULL DEFAULT 0)');
    $db->exec('CREATE TABLE IF NOT EXISTS orders (id TEXT PRIMARY KEY, device TEXT NOT NULL, created INTEGER NOT NULL, paid INTEGER NOT NULL DEFAULT 0, transaction_id TEXT)');
    return $db;
}
function respond(array $v,int $status=200): never {http_response_code($status);header('Content-Type: application/json; charset=utf-8');header('Cache-Control: no-store');echo json_encode($v);exit;}
function device(): string {$v=$_POST['device_id']??'';if(!preg_match('/^[a-f0-9-]{36}$/i',$v))respond(['error'=>'Instalação inválida'],400);return $v;}
function checkInfinite(string $order,string $transaction,string $slug): bool {
    $body=json_encode(['handle'=>INFINITE_HANDLE,'order_nsu'=>$order,'transaction_nsu'=>$transaction,'slug'=>$slug]);
    $ch=curl_init('https://api.checkout.infinitepay.io/payment_check');
    curl_setopt_array($ch,[CURLOPT_POST=>true,CURLOPT_POSTFIELDS=>$body,CURLOPT_HTTPHEADER=>['Content-Type: application/json'],CURLOPT_RETURNTRANSFER=>true,CURLOPT_TIMEOUT=>15]);
    $raw=curl_exec($ch);$http=curl_getinfo($ch,CURLINFO_HTTP_CODE);curl_close($ch);
    $data=json_decode($raw?:'',true);
    return $http===200 && is_array($data) && ($data['success']??false)===true && ($data['paid']??false)===true && ($data['capture_method']??'')==='pix' && (int)($data['amount']??0)===ANNUAL_CENTS;
}
