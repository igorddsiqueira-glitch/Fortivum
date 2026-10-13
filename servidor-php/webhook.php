<?php
require __DIR__.'/config.php';
if($_SERVER['REQUEST_METHOD']!=='POST')respond(['error'=>'Método inválido'],405);
$data=json_decode(file_get_contents('php://input'),true);
if(!is_array($data))respond(['error'=>'JSON inválido'],400);
$order=$data['order_nsu']??'';$transaction=$data['transaction_nsu']??'';$slug=$data['invoice_slug']??($data['slug']??'');
if(!is_string($order)||!is_string($transaction)||!is_string($slug)||!$order||!$transaction||!$slug)respond(['error'=>'Dados incompletos'],400);
$db=db();$stmt=$db->prepare('SELECT device,paid FROM orders WHERE id=?');$stmt->execute([$order]);$row=$stmt->fetch(PDO::FETCH_ASSOC);
if(!$row)respond(['error'=>'Pedido não encontrado'],400);
if((int)$row['paid']===1)respond(['success'=>true,'message'=>null]);
// Não confiar no webhook isoladamente: verificar na InfinitePay e exigir pagamento PIX de R$ 1,00.
if(!checkInfinite($order,$transaction,$slug))respond(['error'=>'Pagamento não confirmado'],400);
$db->beginTransaction();
$upd=$db->prepare('UPDATE orders SET paid=1,transaction_id=? WHERE id=? AND paid=0');$upd->execute([$transaction,$order]);
if($upd->rowCount()===1){
 $stmt=$db->prepare('SELECT expires FROM licenses WHERE device=?');$stmt->execute([$row['device']]);
 $existing=(int)$stmt->fetchColumn();$until=max(time(),$existing)+365*24*60*60;
 $db->prepare('INSERT INTO licenses(device,expires) VALUES(?,?) ON CONFLICT(device) DO UPDATE SET expires=excluded.expires')->execute([$row['device'],$until]);
}
$db->commit();respond(['success'=>true,'message'=>null]);
