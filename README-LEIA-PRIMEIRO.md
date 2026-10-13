# Fortivum — Android + InfinitePay (R$ 1,00 por 365 dias)

## Importante
Este é um **projeto-fonte**, não um APK compilado. O pagamento real ainda não foi testado na conta InfinitePay.
A cobrança é uma renovação **manual**; não existe assinatura recorrente automática.

## Passo 1 — hospedagem
Hospede o conteúdo de `servidor-php/` em `https://loja-com.com/fortivum-api/` com PHP 8.1+, extensões curl e pdo_sqlite, e HTTPS.
`BASE_URL` já está configurado para `https://loja-com.com/fortivum-api`. Confirme que o domínio tem HTTPS ativo e que esse caminho está acessível. Confirme se `INFINITE_HANDLE` corresponde à sua InfiniteTag.
Mantenha a pasta `private` inacessível pela web: a regra `.htaccess` vale para Apache, mas em Nginx configure uma negação explícita. Preferencialmente coloque os dados SQLite fora da pasta pública.
Ative Checkout Integrado no aplicativo/painel InfinitePay. Confira que a conta permite Pix de R$ 1,00 e quais meios de pagamento o checkout oferece.
**O checkout hospedado pode apresentar cartão além de Pix; o servidor só ativa a licença se a InfinitePay confirmar capture_method=pix.** Para impedir que o cliente pague por cartão sem ativação, confirme com a InfinitePay se é possível restringir o checkout exclusivamente a Pix antes de vender.

## Passo 2 — Android
O endereço `https://loja-com.com/fortivum-api` já está configurado no aplicativo.
Não coloque credenciais secretas dentro do aplicativo.

## Passo 3 — compilar só com celular
Crie um repositório **privado** no GitHub e envie o **conteúdo da pasta Fortivum** para a raiz do repositório, incluindo `.github/workflows/android-apk.yml`.
Abra Actions → Compilar APK Android → Run workflow. Após terminar, baixe o artefato `Fortivum-Android-debug`, extraia o ZIP do artefato e instale o APK no Android.
Se for publicar/vender, compile uma versão release assinada com sua chave privada e teste em vários aparelhos. O APK debug é somente para testes.

## Segurança e limitações
- Licença por identificador aleatório local; reinstalar ou limpar dados pode exigir suporte para recuperar acesso. Antes de vender, implemente conta e recuperação de licença.
- Os dados patrimoniais são armazenados localmente **sem criptografia** nesta versão. Não peça dados sensíveis até implementar criptografia e backup seguro.
- O aplicativo consulta o servidor ao abrir; sem internet não é possível validar a licença nesta versão.
- Webhook confirma pedido no servidor por `payment_check`, exigindo Pix e valor exato, e evita ativação duplicada.
- Não é possível garantir aceitação de R$ 1,00 sem testar as regras de valor mínimo da conta e do checkout.
- O projeto não foi compilado nem testado neste ambiente; eventuais erros de Gradle/Android precisam ser corrigidos após a primeira execução do workflow.
- Confira a política de distribuição do canal escolhido para APKs com pagamentos externos.

Documentação oficial: https://www.infinitepay.io/checkout-documentacao

## Instalação no domínio loja-com.com
No gerenciador de arquivos da hospedagem, crie `public_html/fortivum-api/` e envie para lá **o conteúdo** da pasta `servidor-php`, não a pasta inteira. O endereço deve ser `https://loja-com.com/fortivum-api/status.php`. Confirme que a raiz real do domínio é `public_html` (em algumas hospedagens é outra pasta). Verifique PHP 8.1+, PDO SQLite, cURL e HTTPS. Não exponha `private/` na internet.

**Antes de cobrar clientes:** teste a criação de cobrança, Pix de R$ 1,00, webhook, ativação, renovação, e recuperação de licença. O pagamento só libera o acesso após confirmação de Pix; se o checkout permitir cartão, o cliente pode pagar por cartão e não ser liberado. Não publique antes de resolver isso.
