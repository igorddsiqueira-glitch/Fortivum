<?php
require __DIR__.'/config.php';
if($_SERVER['REQUEST_METHOD']!=='POST')respond(['error'=>'Método inválido'],405);
$device=device();$stmt=db()->prepare('SELECT expires FROM licenses WHERE device=?');$stmt->execute([$device]);
$until=(int)$stmt->fetchColumn();respond(['active'=>$until>time(),'valid_until'=>$until]);
