# AutoFiscal IA — cobrança real InfinitePay

## Preparação obrigatória
1. Ative **Checkout Integrado** na InfinitePay.
2. Hospede os arquivos da pasta `server` em `https://SEU-DOMINIO.COM.BR/autofiscal-api/` em hospedagem PHP com HTTPS e extensão cURL.
3. Altere `API_ROOT` em `server/config.php` para o endereço real **sem barra final**.
4. Altere `API_BASE` em `app/src/main/java/br/com/autofiscalia/MainActivity.java` para o mesmo endereço **com barra final**.
5. Garanta que a pasta `server/private` não seja publicamente acessível. Prefira armazenar `STORE` fora do diretório público, alterando a constante no `config.php`. O servidor deve poder escrever no diretório de dados.
6. Envie a pasta `AutoFiscalIA` (seu conteúdo na raiz do repositório) ao GitHub. Em Actions, execute `Compilar APK Android` e baixe o artefato.

## Como testar
- Abra o app e toque **Pagar R$ 9,90**. A InfinitePay abrirá checkout real, com opção Pix quando habilitada.
- Conclua o pagamento. Volte ao app e toque **Verificar pagamento / Restaurar acesso**.
- O webhook chama `payment_check` na InfinitePay e somente marca o pedido como pago se houver confirmação oficial do pedido e do valor.
- Não basta retornar ao aplicativo ou falsificar parâmetros da URL para desbloquear.

## Limitações e segurança antes de publicar
- Este build é um protótipo de pagamento. A licença é associada ao ID local do aparelho; reinstalar o app pode perder o identificador. Para produção, implemente contas de usuário, recuperação de acesso, rate limiting, backup, política de privacidade, termos e controles antifraude.
- O app ainda oferece apenas cálculos ilustrativos e relatórios locais, sem envio à Receita Federal.
- Não armazene documentos fiscais sensíveis no servidor de cobrança.
- Os pagamentos são reais; faça teste somente quando seu servidor estiver configurado e você estiver pronto para pagar.
- `status.php` depende da chegada do webhook. Se a hospedagem bloquear webhooks, não ocorrerá ativação automática.
