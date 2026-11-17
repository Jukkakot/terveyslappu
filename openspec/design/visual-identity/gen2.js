const {chromium}=require('playwright');
const fs=require('fs');
const OUT=__dirname+'/';
const SHOTS='/tmp/claude-0/-home-user-terveyslappu/2ea1c568-cf61-5557-8d73-66f549c8004a/scratchpad/shots/';
const src=fs.readFileSync(OUT+'gen.js','utf8').split('// ---------- render ----------')[0];
const L=new Function('require','__dirname',src+';return {TK,ic,smooth,lineSvg,home,HIST_CSS,page,historia};')(require,__dirname);
const {TK,ic,smooth,home,HIST_CSS,page}=L;

// ---------- tokens ----------
const A={...TK['A-dark'],cf2:'#2A2417',hwF:'#2A2417',hw:'#1B2A27',pill2:'#222827',mu:'#9BAAA5',trend:'#C3CCC8'};
const C={...TK['C-dark'],grid:'rgba(255,255,255,.08)',cf2:'#16191E',hw:'#16191E',hwF:'#16191E',pill2:'#16191E',mu:'#8A939F',trend:'#C4CAD3'};
const AC={...TK['A-dark'],dir:'A',bg:'#0E1013',tx:'#F2F5F7',card:'#16191E',cw:'#16191E',cf:'#16191E',cs:'#16191E',
 tw:'#F2F5F7',tf:'#F2F5F7',ts:'#F2F5F7',mw:'#8E99A4',mf:'#8E99A4',ms:'#8E99A4',
 lw:'#4DF0B8',lwTop:'rgba(77,240,184,.30)',bf:'#4DF0B8',bfp:'#6A5630',bft:'#E2B97A',bs:'#4DF0B8',bsp:'#3B4D6C',
 tileW:'#16352D',tileWf:'#4DF0B8',tileF:'#3A2E17',tileFf:'#F5C16A',tileS:'#1F3250',tileSf:'#9CC3FA',
 subF:'#F5C16A',pill:'#1E2229',pillTx:'#C8CFD8',chipBd:'rgba(255,255,255,.2)',
 fab:'#4DF0B8',fabTx:'#04261C',fab2:'#1E2229',fab2Tx:'#E4EAF0',numS:56,grid:'rgba(255,255,255,.08)',avg:'rgba(255,255,255,.38)',
 acc:'#4DF0B8',accBg:'#1E2229',glow:1,
 cf2:'#221C10',hwF:'#221C10',hw:'#12251F',pill2:'#1C2026',mu:'#93A0AA',trend:'#C4CAD3'};

const EXTRA=`
.phone[style*="--glow:1"] .b1{box-shadow:0 0 22px color-mix(in srgb,var(--fab) 45%,transparent)}
.phone[style*="--glow:1"] .tab.on::after{box-shadow:0 0 10px var(--lw)}
.hero{background:var(--hw)}
.dirC .seg div.on{background:#272C34;box-shadow:none}
.dirC .hero .big{font-size:44px;letter-spacing:-1.5px}
.dirC .hero .big .ms{font-size:34px!important}
.leg{display:flex;gap:14px;font-size:11.5px;color:var(--mu);padding:6px 4px 0;white-space:nowrap;align-items:center}
.leg span{display:inline-flex;align-items:center}
.leg i{display:inline-block;margin-right:5px}
`;

// ---------- helpers ----------
let seed=7; const rnd=()=>{seed=(seed*1664525+1013904223)%4294967296;return seed/4294967296;};
const fmt1=v=>v.toFixed(1).replace('.',',');
const fmtK=v=>String(Math.round(v)).replace(/\B(?=(\d{3})+$)/g,' ');
const xLabels=(padL,w,padR,n)=>{
 const L4=['17.11.25','17.3.','17.7.','17.11.'];
 return L4.map((t,i)=>{const x=padL+i/3*(w-padL-padR);return `<span style="position:absolute;left:${x-24}px;width:48px;text-align:center;font-size:11px;color:var(--mu)${i===3?';color:var(--tx);font-weight:600':''}">${t}</span>`}).join('');
};
const defs=(id)=>`<defs><filter id="${id}" x="-10%" y="-30%" width="120%" height="160%"><feGaussianBlur stdDeviation="3"/></filter></defs>`;

function paino(T){
 const w=304,padL=44,padR=14,H=172,padT=10,padB=8,ymin=78.8,ymax=85.2,N=365;
 const X=d=>padL+d/(N-1)*(w-padL-padR), Y=v=>padT+(1-(v-ymin)/(ymax-ymin))*(H-padT-padB);
 seed=11;
 const data=[];
 for(let d=0;d<N;d++){
  if(d!==N-1&&rnd()<0.14) continue;
  const t=d/(N-1), tr=84-4*(0.9*t+0.1*t*t);
  const n=(rnd()+rnd()-1)*0.8;
  data.push([d,d===N-1?79.9:tr+n]);
 }
 const gs=9, trendAt=d=>{let s=0,ws=0;for(const [dd,v] of data){const k=Math.exp(-((dd-d)**2)/(2*gs*gs));s+=k*v;ws+=k;}return s/ws;};
 const tp=[];for(let d=0;d<N;d+=5)tp.push(d);tp.push(N-1);
 const trendPts=tp.map(d=>[X(d),Y(trendAt(d))]);
 const pts=data.map(([d,v])=>[X(d),Y(v)]);
 const vals=data.map(d=>d[1]);
 const lineD='M'+pts.map(p=>p[0].toFixed(1)+','+p[1].toFixed(1)).join(' L');
 const last=pts[pts.length-1];
 const trendEnd=trendAt(N-1), trendStart=trendAt(0);
 const grid=[84,82,80].map(v=>`<line x1="${padL}" x2="${w-padR}" y1="${Y(v)}" y2="${Y(v)}" style="stroke:var(--grid)" stroke-dasharray="3 4"/>`).join('');
 const id='pf'+T.dir+T.glow;
 const glow=T.glow?`<g filter="url(#${id})" opacity=".35">${pts.map(p=>`<circle cx="${p[0].toFixed(1)}" cy="${p[1].toFixed(1)}" r="1.8" style="fill:var(--lw)"/>`).join('')}</g>`:'';
 const svg=`<svg width="${w}" height="${H}" viewBox="0 0 ${w} ${H}" style="display:block;overflow:visible">${defs(id)}${grid}
  ${glow}
  <path d="${smooth(trendPts)}" fill="none" style="stroke:var(--bg)" stroke-width="5" stroke-linecap="round" opacity=".55"/>
  <path d="${smooth(trendPts)}" fill="none" style="stroke:var(--trend)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
  ${pts.slice(0,-1).map(p=>`<circle cx="${p[0].toFixed(1)}" cy="${p[1].toFixed(1)}" r="1.35" style="fill:var(--lw)"/>`).join('')}
  <line x1="${last[0]}" x2="${last[0]}" y1="4" y2="${H-padB}" style="stroke:var(--lw)" stroke-dasharray="3 3" opacity=".6"/>
  <circle cx="${last[0]}" cy="${last[1]}" r="9" style="fill:var(--lw)" opacity=".2"/>
  <circle cx="${last[0]}" cy="${last[1]}" r="4.5" style="fill:var(--lw);stroke:var(--card)" stroke-width="2.5"/></svg>`;
 const yl=[84,82,80].map(v=>`<div style="position:absolute;left:0;top:${Y(v)-7}px;font-size:11px;color:var(--mu);width:42px;white-space:nowrap">${v} kg</div>`).join('');
 const hc=`<div class="hc"><div class="rngt"><span>17.11.2025 – 17.11.2026</span></div>
  <div style="position:relative;margin-top:6px;height:${H}px">${yl}<div style="position:absolute;left:0;top:0">${svg}</div></div>
  <div style="position:relative;height:18px;margin-top:4px">${xLabels(padL,w,padR)}</div>
  <div class="leg"><span><i style="width:8px;height:8px;border-radius:50%;background:var(--lw)"></i>paino</span><span><i style="width:18px;height:2.5px;border-radius:2px;background:var(--trend)"></i>trendi</span></div>
  <div class="mk" style="right:14px;top:30px"><span style="opacity:.7">Ti 17.11. 7.05</span><b>79,9 kg</b><span style="opacity:.7">trendi ${fmt1(trendEnd)} kg</span></div></div>`;
 const mean=vals.reduce((a,b)=>a+b,0)/vals.length;
 return {hc,stats:{change:trendEnd-trendStart,mean,min:Math.min(...vals),max:Math.max(...vals),n:vals.length}};
}

function frame(T,tab,hcHtml,hero,kv,rows){
 const tabs=['Paino','BMI','Syöty','Askeleet','Kulutus'].map((t,i)=>`<div class="tab${i===tab?' on':''}">${t}</div>`).join('');
 const seg=['1 vk','1 kk','3 kk','1 v','Kaikki','Oma'].map((t,i)=>`<div${i===3?' class="on"':''}>${t}</div>`).join('');
 return `<div class="top" style="height:56px">${ic('arrow_back',24)}<div class="ttl" style="margin-left:4px">Historia</div>${ic('help',24)}${ic('more_vert',24)}</div>
 <div class="tabs">${tabs}</div><div class="seg">${seg}</div>${hcHtml}${hero}${kv}<div class="lst">${rows}</div>`;
}
const more=ic('more_vert',20,0,'').replace('class="ms"','class="ms more"');

function historiaPaino(T){
 const {hc,stats:s}=paino(T);
 const hero=`<div class="hero"><div><div class="lab">Muutos</div><div class="big">${ic('arrow_downward',30,0,'')}−${fmt1(Math.abs(s.change))}<small>kg</small></div></div><div class="side">Kuukaudessa<b>↓ −${(Math.abs(s.change)/12).toFixed(2).replace('.',',')} kg</b></div></div>`;
 const kv=`<div class="kv"><div>Keskiarvo<b>${fmt1(s.mean)} kg</b></div><div>Alin<b>${fmt1(s.min)} kg</b></div><div>Ylin<b>${fmt1(s.max)} kg</b></div><div>Painoja<b>${s.n}</b></div></div>`;
 const rows=[['Tänään 7.05','↓','−0,3 kg','79,9 kg'],['Eilen 7.05','↓','−0,3 kg','80,2 kg'],['Su 15.11. 7.05','↑','+0,1 kg','80,4 kg'],['Pe 13.11. 7.05','↑','+0,1 kg','80,3 kg']]
  .map(([d,a,c,k])=>`<div class="li"><span class="d">${d}</span><span class="ch">${ic(a==='↓'?'arrow_downward':'arrow_upward',15)+c}</span><span class="kg">${k}</span>${more}</div>`).join('');
 return frame(T,0,hc,hero,kv,rows);
}

function historiaSyoty(T){
 const w=304,padL=48,padR=10,h=160,top=14,W=52,max=1500;
 const plotW=w-padL-padR,slot=plotW/W,bw=Math.max(2.6,slot-1.6);
 seed=23;
 const wk=[],tr=[];
 for(let i=0;i<W;i++){
  const kcal=i===W-1?944:900+95*Math.sin(i/6.5)+(rnd()+rnd()-1)*230;
  const treat=i===W-1?33:30+rnd()*120;
  wk.push([kcal,treat]);
 }
 const sm=i=>{let s=0,ws=0;for(let j=0;j<W;j++){const k=Math.exp(-((i-j)**2)/(2*3*3));s+=k*wk[j][0];ws+=k;}return s/ws;};
 const gy=v=>h-1-(v/max)*(h-6);
 let bars='';
 wk.forEach(([k,t],i)=>{
  const x=i*slot+(slot-bw)/2, y=gy(k), yt=gy(k-t), cur=i===W-1, r=Math.min(1.5,bw/2);
  const path=`M${x.toFixed(1)},${h-1} L${x.toFixed(1)},${(y+r).toFixed(1)} Q${x.toFixed(1)},${y.toFixed(1)} ${(x+r).toFixed(1)},${y.toFixed(1)} L${(x+bw-r).toFixed(1)},${y.toFixed(1)} Q${(x+bw).toFixed(1)},${y.toFixed(1)} ${(x+bw).toFixed(1)},${(y+r).toFixed(1)} L${(x+bw).toFixed(1)},${h-1} Z`;
  bars+=`<path d="${path}" style="fill:var(--bf${cur?'':'p'})"/>`;
  bars+=`<rect x="${x.toFixed(1)}" y="${y.toFixed(1)}" width="${bw.toFixed(1)}" height="${(yt-y).toFixed(1)}" style="fill:var(--bft)" ${cur?'':'opacity=".6"'}/>`;
 });
 const tp=[];for(let i=0;i<W;i+=2)tp.push(i);if(tp[tp.length-1]!==W-1)tp.push(W-1);
 const trendPts=tp.map(i=>[(i+.5)*slot,gy(sm(i))]);
 const cx=(W-.5)*slot;
 const svg=`<svg width="${plotW}" height="${h}" viewBox="0 0 ${plotW} ${h}" style="display:block;overflow:visible">
  <line x1="0" x2="${plotW}" y1="${h-.5}" y2="${h-.5}" style="stroke:var(--grid)"/>${bars}
  <path d="${smooth(trendPts)}" fill="none" style="stroke:var(--bg)" stroke-width="5" stroke-linecap="round" opacity=".6"/>
  <path d="${smooth(trendPts)}" fill="none" style="stroke:var(--trend)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
  <line x1="${cx}" x2="${cx}" y1="0" y2="${h}" style="stroke:var(--bf)" stroke-dasharray="3 3" opacity=".7"/></svg>`;
 const yl=[0,750,1500].map(v=>`<div style="position:absolute;left:0;top:${gy(v)-7+top}px;font-size:11px;color:var(--mu);width:46px;white-space:nowrap">${fmtK(v)} kcal</div>`+(v?`<div style="position:absolute;left:${padL}px;right:${padR}px;top:${gy(v)+top}px;border-top:1px dashed var(--grid)"></div>`:'')).join('');
 const sw=c=>`<i style="width:10px;height:10px;border-radius:3px;background:var(${c})"></i>`;
 const hc=`<div class="hc"><div class="rngt"><span>17.11.2025 – 17.11.2026</span></div>
  <div style="position:relative;margin-top:6px;height:${h+top}px">${yl}<div style="position:absolute;left:${padL}px;top:${top}px">${svg}</div></div>
  <div style="position:relative;height:18px;margin-top:12px">${xLabels(padL+0,w,padR)}</div>
  <div class="leg"><span>${sw('--bf')}syöty</span><span>${sw('--bft')}herkut</span><span><i style="width:16px;height:2.5px;border-radius:2px;background:var(--trend)"></i>trendi</span><span style="opacity:.85"><i style="width:10px;height:8px;border-bottom:2px solid var(--mu);border-left:2px solid var(--mu);border-radius:1px"></i>viikoittain</span></div>
  <div class="mk" style="right:14px;top:26px"><span style="opacity:.7">Vk 46 · 11.–17.11.</span><b>944 kcal/pv</b></div></div>`;
 const mean=wk.reduce((a,b)=>a+b[0],0)/W, tmean=wk.reduce((a,b)=>a+b[1],0)/W;
 const hero=`<div class="hero" style="background:var(--hwF)"><div><div class="lab">Keskiarvo</div><div class="big">${fmtK(mean)}<small>kcal/pv</small></div></div></div>`;
 const kv=`<div class="kv"><div>Herkut<b>${fmtK(tmean)} kcal/pv</b></div><div>Päiviä<b>341 / 365</b></div></div>`;
 const rows=[['Vk 46 · 11.–17.11.','944 kcal/pv'],['Vk 45 · 4.–10.11.','1 012 kcal/pv'],['Vk 44 · 28.10.–3.11.','987 kcal/pv'],['Vk 43 · 21.–27.10.','921 kcal/pv']]
  .map(([d,k])=>`<div class="li"><span class="d">${d}</span><span class="kg">${k}</span>${more}</div>`).join('');
 return frame(T,2,hc,hero,kv,rows);
}

// ---------- render ----------
const frames=[];
const add=(file,cap,T,body,css)=>frames.push({file,html:page(cap,T,body,css)});
add('AC-etusivu-dark','AC · Etusivu · tumma',AC,
 home(AC).replace(/<span class="mu" style="font-size:1[12.5]*px">ka ([\d ]+) \/pv<\/span>/g,(m,n)=>`<span class="mu" style="font-size:11px;text-align:right;line-height:1.2">keskiarvo<br>${n} /pv</span>`),EXTRA);
for(const [n,T] of [['A',A],['C',C],['AC',AC]]){
 add(`${n}-historia-paino-dark`,`${n} · Historia · Paino · 1 v · tumma`,T,historiaPaino(T),HIST_CSS+EXTRA);
 add(`${n}-historia-syoty-dark`,`${n} · Historia · Syöty · 1 v · tumma`,T,historiaSyoty(T),HIST_CSS+EXTRA);
}

(async()=>{
 const b=await chromium.launch();
 const ctx=await b.newContext({viewport:{width:360,height:808},deviceScaleFactor:2});
 const p=await ctx.newPage();
 for(const f of frames){
  fs.writeFileSync(OUT+f.file+'.html',f.html);
  await p.setContent(f.html,{waitUntil:'networkidle'});
  await p.evaluate(()=>document.fonts.ready);await p.waitForTimeout(250);
  await p.screenshot({path:OUT+f.file+'.png',clip:{x:0,y:0,width:360,height:808}});
  await p.screenshot({path:OUT+f.file+'-body.png',clip:{x:0,y:28,width:360,height:780}});
 }
 const cmp=async(name,curSrc,l1,newFile,l2)=>{
  const html=`<!doctype html><html><head><meta charset="utf-8"><style>*{margin:0}body{background:#2a2c2e;display:flex;gap:32px;padding:32px;width:1536px;font-family:Roboto,sans-serif}
  .f{width:720px}.c{color:#fff;font:600 34px/56px Roboto,sans-serif;height:64px}.f img{width:720px;height:1560px;display:block}</style></head><body>
  <div class="f"><div class="c">${l1}</div><img src="file://${curSrc}"></div><div class="f"><div class="c">${l2}</div><img src="file://${OUT+newFile}-body.png"></div></body></html>`;
  fs.writeFileSync(OUT+name+'.html',html);
  const pg=await b.newPage({viewport:{width:1536,height:1688},deviceScaleFactor:1});
  await pg.goto('file://'+OUT+name+'.html');await pg.waitForTimeout(300);
  await pg.screenshot({path:OUT+name+'.png',clip:{x:0,y:0,width:1536,height:1688}});await pg.close();
 };
 const H=SHOTS+'01-home-dark.png',P=SHOTS+'10-history-paino-dark.png',S=SHOTS+'11-history-energia-dark.png';
 await cmp('vertailu-etusivu-AC',H,'Nykyinen','AC-etusivu-dark','AC');
 await cmp('vertailu-historia-paino-A',P,'Nykyinen','A-historia-paino-dark','A');
 await cmp('vertailu-historia-paino-C',P,'Nykyinen','C-historia-paino-dark','C');
 await cmp('vertailu-historia-paino-AC',P,'Nykyinen','AC-historia-paino-dark','AC');
 await cmp('vertailu-historia-syoty-C',S,'Nykyinen','C-historia-syoty-dark','C');
 await cmp('vertailu-historia-syoty-AC',S,'Nykyinen','AC-historia-syoty-dark','AC');
 await b.close();
})().catch(e=>{console.error(e);process.exit(1)});
