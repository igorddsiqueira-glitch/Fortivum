package com.fortivum.patrimonio;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.text.*;
import java.util.*;
import java.net.*;
import java.io.*;
import android.os.Handler;

public class MainActivity extends Activity {
  final int BG=Color.rgb(13,17,25), PANEL=Color.rgb(25,31,43), GOLD=Color.rgb(215,176,92), WHITE=Color.rgb(246,246,242), MUTED=Color.rgb(163,174,191);
  static final String PIX_FALLBACK="00020126580014br.gov.bcb.pix013665861fdf-adfb-4ac4-bc66-5207fd400bd052040000530398654049.905802BR5924Misael Nascimento  Do Na6009Sao Paulo62240520daqr12780523143765366304AACB";
  String pixCode(){
    try(InputStream in=getAssets().open("pix/copia_e_cola.txt");ByteArrayOutputStream out=new ByteArrayOutputStream()){
      byte[] buf=new byte[1024];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);
      String value=out.toString("UTF-8").trim();return value.isEmpty()?PIX_FALLBACK:value;
    }catch(Exception e){return PIX_FALLBACK;}
  }
  static final String SUPPORT_EMAIL="igorddsiqueira85@gmail.com";
  static final String API_BASE="https://loja-com.com/fortivum-api"; // SUBSTITUIR PELO SEU HTTPS
  final Handler handler=new Handler();
  String deviceId;
  long validUntil=0;
  LinearLayout root, content; JSONArray items=new JSONArray(); boolean hidden=false;
  String[] types={"Imóvel","Veículo","Investimento","Outros bens","Dívida"};
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);load();deviceId=getPreferences(0).getString("deviceId", "");if(deviceId.isEmpty()){deviceId=UUID.randomUUID().toString();getPreferences(0).edit().putString("deviceId",deviceId).apply();}checkLicense();}
  void load(){try{items=new JSONArray(getPreferences(0).getString("items","[]"));hidden=getPreferences(0).getBoolean("hidden",false);}catch(Exception e){items=new JSONArray();}}
  void save(){getPreferences(0).edit().putString("items",items.toString()).putBoolean("hidden",hidden).apply();}
  GradientDrawable shape(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);return g;}
  TextView text(String s,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(null,Typeface.BOLD);return v;}
  LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  void pad(View v,int p){v.setPadding(p,p,p,p);}
  String money(double v){return NumberFormat.getCurrencyInstance(new Locale("pt","BR")).format(v);}
  double sum(String t){double total=0;for(int i=0;i<items.length();i++){JSONObject o=items.optJSONObject(i);if(o!=null && t.equals(o.optString("type")))total+=o.optDouble("value");}return total;}
  double assets(){double a=0;for(String t:types)if(!t.equals("Dívida"))a+=sum(t);return a;}
  String shown(double v){return hidden?"••••••":money(v);}
  void checkLicense(){
    if(API_BASE.contains("SEU-DOMINIO")){showPaywall("Configure o endereço do servidor para ativar o Pix.");return;}
    api("/status.php", "device_id="+deviceId, (json)->{
      validUntil=json.optLong("valid_until",0);
      if(json.optBoolean("active",false)){render();}else showPaywall("Para ativar seu acesso anual, pague R$ 9,90 via Pix.");
    });
  }
  interface Callback { void done(JSONObject obj); }
  void api(String path,String body,Callback callback){
    new Thread(()->{
      JSONObject result=new JSONObject();
      HttpURLConnection conn=null;
      try{
        URL endpoint=new URL(API_BASE+path);
        conn=(HttpURLConnection)endpoint.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        conn.setRequestProperty("Accept","application/json");
        try(OutputStream out=conn.getOutputStream()){
          out.write(body.getBytes("UTF-8"));
        }
        int code=conn.getResponseCode();
        InputStream stream=code>=200 && code<300 ? conn.getInputStream() : conn.getErrorStream();
        if(stream==null){
          result.put("error","Servidor respondeu HTTP "+code+" sem detalhes.");
        }else{
          try(InputStream in=stream;ByteArrayOutputStream buf=new ByteArrayOutputStream()){
            byte[] chunk=new byte[2048];int n;
            while((n=in.read(chunk))!=-1)buf.write(chunk,0,n);
            String response=buf.toString("UTF-8").trim();
            try{
              result=new JSONObject(response);
              if(code<200 || code>=300){
                String error=result.optString("error","Erro do servidor");
                result.put("error",error+" (HTTP "+code+")");
              }
            }catch(org.json.JSONException badJson){
              result=new JSONObject();
              result.put("error","Resposta inválida do servidor (HTTP "+code+").");
            }
          }
        }
      }catch(Exception ex){
        try{result.put("error","Falha de conexão: "+ex.getClass().getSimpleName()+". Verifique a internet e o servidor.");}
        catch(Exception ignored){}
      }finally{if(conn!=null)conn.disconnect();}
      final JSONObject output=result;
      runOnUiThread(()->callback.done(output));
    }).start();
  }
  final int BLUE=Color.rgb(24,93,202), NAVY=Color.rgb(14,32,62), LIGHT=Color.rgb(243,246,252), INK=Color.rgb(19,41,75), GRAY=Color.rgb(81,101,133);
  void paymentPage(String heading, String subtitle){
    ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(LIGHT);
    root=vertical();sc.addView(root);setContentView(sc);
    LinearLayout header=vertical();header.setPadding(24,36,24,30);header.setBackgroundColor(NAVY);root.addView(header);
    header.addView(text(heading,29,Color.WHITE,true));spaceIn(header,12);
    header.addView(text(subtitle,17,Color.rgb(191,207,232),false));
  }
  LinearLayout paymentCard(String title,String description){
    LinearLayout card=vertical();card.setPadding(18,20,18,20);card.setBackground(shape(Color.WHITE,20));
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(20,18,20,0);root.addView(card,lp);
    card.addView(text(title,22,INK,true));
    if(description!=null&&!description.isEmpty()){spaceIn(card,13);card.addView(text(description,17,GRAY,false));}
    return card;
  }
  Button paymentButton(String label){
    Button b=new Button(this);b.setText(label);b.setTextSize(17);b.setAllCaps(false);b.setTextColor(Color.WHITE);
    b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(BLUE));return b;
  }
  void paymentAction(String label, Runnable action){
    Button b=paymentButton(label);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
    lp.setMargins(25,20,25,0);root.addView(b,lp);b.setOnClickListener(v->action.run());
  }
  void showPaywall(String message){
    paymentPage("FORTIVUM", "Ative seu acesso anual");
    paymentCard("Plano anual • R$ 9,90", "Acesso ao Fortivum por 365 dias após a confirmação do pagamento.");
    paymentCard("Pagamento por Pix", "Pague com o código Pix Copia e Cola usando o aplicativo do seu banco.");
    paymentAction("Pagar R$ 9,90",()->showPix());
    paymentAction("Já realizei o pagamento",()->showPaidMessage());
  }
  void showPix(){
    paymentPage("Pagamento por Pix", "Plano anual • R$ 9,90");
    LinearLayout card=paymentCard("Como pagar", "1. Toque em Copiar código Pix.\n\n2. Abra o aplicativo do seu banco.\n\n3. Entre em Pix e selecione Pix Copia e Cola.\n\n4. Cole o código, confira o nome do recebedor e o valor de R$ 9,90.\n\n5. Confirme o pagamento no banco.");
    spaceIn(card,20);
    TextView code=text(pixCode(),12,GRAY,false);code.setTextIsSelectable(true);card.addView(code);
    paymentAction("Copiar código Pix",()->{
      android.content.ClipboardManager cm=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
      cm.setPrimaryClip(ClipData.newPlainText("Pix Fortivum",pixCode()));
      Toast.makeText(this,"Código Pix copiado!",Toast.LENGTH_SHORT).show();
    });
    paymentAction("Já realizei o pagamento",()->showPaidMessage());
    paymentAction("Voltar",()->showPaywall(null));
  }
  void showPaidMessage(){
    paymentPage("Pagamento realizado", "Confirmação do acesso anual");
    paymentCard("Fale com o suporte", "Se você já pagou, entre em contato para confirmar o pagamento e solicitar a liberação do seu acesso.\n\n"+SUPPORT_EMAIL);
    paymentAction("Enviar e-mail ao suporte",()->contactSupport());
    paymentAction("Voltar",()->showPaywall(null));
  }
  void contactSupport(){
    Intent email=new Intent(Intent.ACTION_SENDTO);
    email.setData(android.net.Uri.parse("mailto:"+SUPPORT_EMAIL));
    email.putExtra(Intent.EXTRA_SUBJECT,"Fortivum - pagamento Pix / liberação de acesso");
    email.putExtra(Intent.EXTRA_TEXT,"Olá, realizei o Pix de R$ 9,90 e preciso confirmar minha assinatura Fortivum.\nIdentificador do app: "+deviceId+"\nAnexo o comprovante de pagamento.");
    try{startActivity(email);}catch(android.content.ActivityNotFoundException ex){
      Toast.makeText(this,"Envie um e-mail para "+SUPPORT_EMAIL,Toast.LENGTH_LONG).show();
    }
  }
  void render(){
    ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(BG);root=vertical();pad(root,22);sc.addView(root);setContentView(sc);
    TextView logo=text("◆  FORTIVUM",24,GOLD,true);root.addView(logo);
    TextView sub=text("PATRIMÔNIO SOB CONTROLE",11,MUTED,false);sub.setLetterSpacing(.14f);root.addView(sub);
    space(25);
    LinearLayout summary=vertical();pad(summary,22);summary.setBackground(shape(PANEL,24));root.addView(summary);
    summary.addView(text("PATRIMÔNIO LÍQUIDO",12,MUTED,true));spaceIn(summary,9);
    summary.addView(text(shown(assets()-sum("Dívida")),31,GOLD,true));spaceIn(summary,14);
    summary.addView(text("Bens e investimentos: "+shown(assets()),14,WHITE,false));
    summary.addView(text("Dívidas: "+shown(sum("Dívida")),14,MUTED,false));
    space(15);
    Button toggle=button(hidden?"Mostrar valores":"Ocultar valores");root.addView(toggle);toggle.setOnClickListener(v->{hidden=!hidden;save();render();});
    space(18);root.addView(text("Distribuição patrimonial",20,WHITE,true));space(12);
    for(String t:types){double amount=sum(t);if(amount>0){LinearLayout r=vertical();pad(r,12);r.setBackground(shape(PANEL,18));root.addView(r);r.addView(text(t+"  •  "+shown(amount),15,WHITE,true));if(!t.equals("Dívida")&&assets()>0){ProgressBar pb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);pb.setMax(100);pb.setProgress((int)Math.round(100*amount/assets()));r.addView(pb);}space(7);}}
    space(18);Button add=button("+ Adicionar bem ou dívida");root.addView(add);add.setOnClickListener(v->addDialog());
    space(23);root.addView(text("Meus registros",20,WHITE,true));space(10);
    if(items.length()==0){root.addView(text("Nenhum registro ainda. Cadastre seu primeiro bem para começar.",14,MUTED,false));}
    for(int i=0;i<items.length();i++){JSONObject o=items.optJSONObject(i);if(o==null)continue;final int idx=i;LinearLayout r=vertical();pad(r,16);r.setBackground(shape(PANEL,16));root.addView(r);r.addView(text(o.optString("name"),17,WHITE,true));r.addView(text(o.optString("type")+"  •  "+shown(o.optDouble("value")),14,GOLD,false));r.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(o.optString("name")).setMessage("Excluir este registro?").setNegativeButton("Cancelar",null).setPositiveButton("Excluir",(d,w)->{JSONArray next=new JSONArray();for(int j=0;j<items.length();j++)if(j!=idx)next.put(items.opt(j));items=next;save();render();}).show());space(8);}
    space(16);root.addView(text("Licença até: "+new java.text.SimpleDateFormat("dd/MM/yyyy",new Locale("pt","BR")).format(new Date(validUntil*1000)),12,GOLD,false));
    space(24);root.addView(text("Dados salvos apenas neste aparelho. Faça backup antes de desinstalar.",12,MUTED,false));
    space(8);Button export=button("Compartilhar backup JSON");root.addView(export);export.setOnClickListener(v->{Intent intent=new Intent(Intent.ACTION_SEND);intent.setType("text/plain");intent.putExtra(Intent.EXTRA_TEXT,items.toString());startActivity(Intent.createChooser(intent,"Salvar backup"));});
    space(8);Button restore=button("Restaurar backup JSON");root.addView(restore);restore.setOnClickListener(v->restoreDialog());
  }
  Button button(String label){Button b=new Button(this);b.setText(label);b.setTextColor(BG);b.setAllCaps(false);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(GOLD));return b;}
  void space(int h){View v=new View(this);root.addView(v,new LinearLayout.LayoutParams(1,h));}
  void spaceIn(LinearLayout l,int h){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,h));}
  void addDialog(){LinearLayout form=vertical();pad(form,20);EditText name=new EditText(this);name.setHint("Descrição (ex.: Apartamento Centro)");form.addView(name);Spinner spinner=new Spinner(this);spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,types));form.addView(spinner);EditText val=new EditText(this);val.setHint("Valor em R$ (ex.: 250000,50)");val.setInputType(8194);form.addView(val);
    new AlertDialog.Builder(this).setTitle("Novo registro").setView(form).setNegativeButton("Cancelar",null).setPositiveButton("Salvar",(d,w)->{try{String n=name.getText().toString().trim();double amount=Double.parseDouble(val.getText().toString().trim().replace(".","").replace(",","."));if(n.isEmpty()||amount<=0||Double.isInfinite(amount)||Double.isNaN(amount))throw new Exception();JSONObject o=new JSONObject();o.put("name",n);o.put("type",types[spinner.getSelectedItemPosition()]);o.put("value",amount);items.put(o);save();render();}catch(Exception e){Toast.makeText(this,"Informe descrição e valor positivo válido",Toast.LENGTH_LONG).show();}}).show();
  }
  void restoreDialog(){EditText input=new EditText(this);input.setHint("Cole aqui o conteúdo JSON do backup");new AlertDialog.Builder(this).setTitle("Restaurar backup").setMessage("Substituirá os registros atuais.").setView(input).setNegativeButton("Cancelar",null).setPositiveButton("Restaurar",(d,w)->{try{JSONArray a=new JSONArray(input.getText().toString());for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);if(o.optString("name").trim().isEmpty()||!Arrays.asList(types).contains(o.optString("type"))||o.optDouble("value")<=0)throw new Exception();}items=a;save();render();}catch(Exception e){Toast.makeText(this,"Backup inválido",Toast.LENGTH_LONG).show();}}).show();}
}
