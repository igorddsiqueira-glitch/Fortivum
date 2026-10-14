<?php
require __DIR__.'/config.php';$d=input();$id=$d['order_nsu']??'';$tx=$d['transaction_nsu']??'';$slug=$d['invoice_slug']??($d['slug']??'');
if(!is_string($id)||!is_string($tx)||!is_string($slug)||!$id||!$tx||!$slug)reply(['success'=>false,'message'=>'Dados ausentes'],400);
$order=locked(function(&$rows)use($id){return $rows[$id]??null;});if(!$order)reply(['success'=>false,'message'=>'Pedido não encontrado'],400);
$order['transaction_nsu']=$tx;$order['slug']=$slug;
if(!verify_order($order))reply(['success'=>false,'message'=>'Pagamento não confirmado'],400);
locked(function(&$rows)use($id,$tx,$slug){if(!isset($rows[$id]))return;$rows[$id]['transaction_nsu']=$tx;$rows[$id]['slug']=$slug;if(empty($rows[$id]['expires']))$rows[$id]['expires']=time()+365*86400;});reply(['success'=>true,'message'=>null]);
