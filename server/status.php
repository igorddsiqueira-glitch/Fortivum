<?php
require __DIR__.'/config.php';$d=input();$device=valid_device($d);
$expiration=locked(function(&$rows)use($device){$max=0;foreach($rows as $row){if(($row['device']??'')===$device)$max=max($max,(int)($row['expires']??0));}return $max;});
reply(['ok'=>true,'active'=>$expiration>time(),'expires'=>$expiration]);
