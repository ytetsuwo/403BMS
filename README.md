##*403 Book Management System*##

このプロジェクトは、403研究室用の図書管理システムです。  
PostgreSQLを利用して管理します。  

現在、以下のコマンドに対応しています。
  
     adduser [username]     ユーザーを追加します
     rmuser [username]      ユーザーを削除します

     add [ISBN]             本を追加します  
     inf  
     rm または remove [ISBN] 本を削除します 
     b  または borrow [ISBN] 本を借ります  
     r  または return [ISBN] 本を返します 
     ls または list          データベースの内容を表示します
     st または status        貸出中の本を一覧表示します
     h  または help          ヘルプを表示します
     clsまたは clear         画面をクリアします
     q  または exit          管理システムを終了します

以下のコマンドで実行できます

    java -jar 403BookManagementSystem/ExportedJAR/403BMS.jar
