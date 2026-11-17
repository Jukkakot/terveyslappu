const {chromium}=require('playwright');
const fs=require('fs');
const OUT=__dirname+'/';
const SHOTS='/tmp/claude-0/-home-user-terveyslappu/2ea1c568-cf61-5557-8d73-66f549c8004a/scratchpad/shots/';

// ---------- tokens ----------
const TK={
 'A-light':{dir:'A',bg:'#EEF1F0',tx:'#151A19',card:'#FAFBFA',cw:'#E3EFEB',cf:'#FAFBFA',cs:'#FAFBFA',
  tw:'#151A19',tf:'#151A19',ts:'#151A19',mw:'#586661',mf:'#586661',ms:'#586661',
  lw:'#2E7D6B',lwTop:'rgba(46,125,107,.32)',bf:'#D48A12',bfp:'#EBCF9B',bft:'#8A5A44',bs:'#3D6FC0',bsp:'#BACBEA',
  tileW:'#CFE6DF',tileWf:'#1F6B5A',tileF:'#F6E3BF',tileFf:'#96590A',tileS:'#D8E4F7',tileSf:'#2F5CA5',
  subF:'#96590A',chipBd:'rgba(0,0,0,.18)',chipBg:'transparent',pill:'#E3EFEB',pillTx:'#174F43',
  fab:'#2E7D6B',fabTx:'#FFFFFF',fab2:'#DCEBE6',fab2Tx:'#174F43',fab2Bd:'transparent',
  r:24,numW:600,numS:52,grid:'rgba(0,0,0,.08)',avg:'rgba(0,0,0,.35)',acc:'#1F6B5A',accBg:'#DCEBE6',scrim:'rgba(0,0,0,.5)',glow:0},
 'A-dark':{dir:'A',bg:'#101312',tx:'#E6ECEA',card:'#1A1E1D',cw:'#1B2A27',cf:'#1A1E1D',cs:'#1A1E1D',
  tw:'#E6ECEA',tf:'#E6ECEA',ts:'#E6ECEA',mw:'#9BAAA5',mf:'#9BAAA5',ms:'#9BAAA5',
  lw:'#5FCDB2',lwTop:'rgba(95,205,178,.34)',bf:'#F2B450',bfp:'#6A5630',bft:'#D68C70',bs:'#86B4F5',bsp:'#36496B',
  tileW:'#24403A',tileWf:'#86E0C8',tileF:'#44361A',tileFf:'#F5C16A',tileS:'#243653',tileSf:'#9CC3FA',
  subF:'#F5C16A',chipBd:'rgba(255,255,255,.22)',chipBg:'transparent',pill:'#24403A',pillTx:'#A7EBD8',
  fab:'#5FCDB2',fabTx:'#07251E',fab2:'#27302D',fab2Tx:'#D2EAE3',fab2Bd:'transparent',
  r:24,numW:600,numS:52,grid:'rgba(255,255,255,.08)',avg:'rgba(255,255,255,.4)',acc:'#6FD6BC',accBg:'#24403A',scrim:'rgba(0,0,0,.62)',glow:0},
 'B-light':{dir:'B',bg:'#F3F2EE',tx:'#1B1C1A',card:'#FFFFFF',cw:'#C9E9DE',cf:'#FBE1AB',cs:'#CDDFFA',
  tw:'#0A3A2F',tf:'#3D2800',ts:'#0A2A57',mw:'#2B5B4F',mf:'#6A4800',ms:'#244D8A',
  lw:'#0A3A2F',lwTop:'rgba(10,58,47,.26)',bf:'#3D2800',bfp:'rgba(61,40,0,.30)',bft:'#A8561F',bs:'#0A2A57',bsp:'rgba(10,42,87,.30)',
  tileW:'rgba(255,255,255,.6)',tileWf:'#0A3A2F',tileF:'rgba(255,255,255,.6)',tileFf:'#3D2800',tileS:'rgba(255,255,255,.6)',tileSf:'#0A2A57',
  subF:'#7A3E12',chipBd:'transparent',chipBg:'rgba(255,255,255,.6)',pill:'rgba(255,255,255,.6)',pillTx:'#0A3A2F',
  fab:'#0F5B4C',fabTx:'#FFFFFF',fab2:'#E4E2DB',fab2Tx:'#1B1C1A',fab2Bd:'transparent',
  r:28,numW:700,numS:48,grid:'rgba(10,58,47,.10)',avg:'rgba(10,58,47,.45)',acc:'#0F5B4C',accBg:'#C9E9DE',scrim:'rgba(0,0,0,.5)',glow:0},
 'B-dark':{dir:'B',bg:'#0F1211',tx:'#E8EBEA',card:'#1A1E1D',cw:'#18463B',cf:'#4A3811',cs:'#1E3B66',
  tw:'#C9F1E4',tf:'#FCE3AC',ts:'#D3E3FC',mw:'#9FD6C5',mf:'#E2C98E',ms:'#A6C3F0',
  lw:'#C9F1E4',lwTop:'rgba(201,241,228,.30)',bf:'#FCE3AC',bfp:'rgba(252,227,172,.30)',bft:'#F0A173',bs:'#D3E3FC',bsp:'rgba(211,227,252,.30)',
  tileW:'rgba(255,255,255,.14)',tileWf:'#C9F1E4',tileF:'rgba(255,255,255,.14)',tileFf:'#FCE3AC',tileS:'rgba(255,255,255,.14)',tileSf:'#D3E3FC',
  subF:'#F6C48F',chipBd:'transparent',chipBg:'rgba(255,255,255,.14)',pill:'rgba(255,255,255,.14)',pillTx:'#C9F1E4',
  fab:'#BFEBDD',fabTx:'#06241C',fab2:'#2A302E',fab2Tx:'#E8EBEA',fab2Bd:'transparent',
  r:28,numW:700,numS:48,grid:'rgba(255,255,255,.10)',avg:'rgba(255,255,255,.5)',acc:'#C9F1E4',accBg:'#18463B',scrim:'rgba(0,0,0,.62)',glow:0},
 'C-dark':{dir:'C',bg:'#0E1013',tx:'#F4F6F8',card:'#16191E',cw:'#16191E',cf:'#16191E',cs:'#16191E',
  tw:'#F4F6F8',tf:'#F4F6F8',ts:'#F4F6F8',mw:'#8A939F',mf:'#8A939F',ms:'#8A939F',
  lw:'#4DF0B8',lwTop:'rgba(77,240,184,.30)',bf:'#4DF0B8',bfp:'#685943',bft:'#E2B97A',bs:'#4DF0B8',bsp:'#46556F',
  tileW:'transparent',tileWf:'#4DF0B8',tileF:'transparent',tileFf:'#E2B97A',tileS:'transparent',tileSf:'#8FB0E8',
  subF:'#E2B97A',chipBd:'rgba(255,255,255,.16)',chipBg:'transparent',pill:'#1E2229',pillTx:'#C8CFD8',
  fab:'#4DF0B8',fabTx:'#04261C',fab2:'transparent',fab2Tx:'#F4F6F8',fab2Bd:'rgba(255,255,255,.22)',
  r:24,numW:600,numS:64,grid:'rgba(255,255,255,0)',avg:'rgba(255,255,255,.35)',acc:'#4DF0B8',accBg:'#1E2229',scrim:'rgba(0,0,0,.6)',glow:1},
 'C-light':{dir:'C',bg:'#F1F2F4',tx:'#101216',card:'#FFFFFF',cw:'#FFFFFF',cf:'#FFFFFF',cs:'#FFFFFF',
  tw:'#101216',tf:'#101216',ts:'#101216',mw:'#5E6772',mf:'#5E6772',ms:'#5E6772',
  lw:'#0FB585',lwTop:'rgba(15,181,133,.26)',bf:'#0FB585',bfp:'#E4D2AE',bft:'#A9741C',bs:'#0FB585',bsp:'#C0CEE8',
  tileW:'transparent',tileWf:'#0A8F69',tileF:'transparent',tileFf:'#9A6812',tileS:'transparent',tileSf:'#3F65AE',
  subF:'#9A6812',chipBd:'rgba(0,0,0,.16)',chipBg:'transparent',pill:'#EEF0F3',pillTx:'#2B323B',
  fab:'#2BE0A9',fabTx:'#05261C',fab2:'transparent',fab2Tx:'#101216',fab2Bd:'rgba(0,0,0,.22)',
  r:24,numW:600,numS:64,grid:'rgba(0,0,0,0)',avg:'rgba(0,0,0,.3)',acc:'#0A8F69',accBg:'#EEF0F3',scrim:'rgba(0,0,0,.5)',glow:1},
};

// ---------- helpers ----------
const ic=(n,s=24,fill=0,extra='')=>`<span class="ms" style="font-size:${s}px;font-variation-settings:'FILL' ${fill},'wght' 400,'opsz' 24;${extra}">${n}</span>`;

function cssVars(T){
 return Object.entries(T).filter(([k,v])=>typeof v!=='object').map(([k,v])=>`--${k}:${v}${typeof v==='number'&&['r','numS'].includes(k)?'px':''}`).join(';');
}

function smooth(pts){
 let d=`M${pts[0][0]},${pts[0][1]}`;
 for(let i=0;i<pts.length-1;i++){
  const p0=pts[i-1]||pts[i],p1=pts[i],p2=pts[i+1],p3=pts[i+2]||p2;
  const t=0.18;
  const c1=[p1[0]+(p2[0]-p0[0])*t,p1[1]+(p2[1]-p0[1])*t];
  const c2=[p2[0]-(p3[0]-p1[0])*t,p2[1]-(p3[1]-p1[1])*t];
  d+=` C${c1[0].toFixed(1)},${c1[1].toFixed(1)} ${c2[0].toFixed(1)},${c2[1].toFixed(1)} ${p2[0].toFixed(1)},${p2[1].toFixed(1)}`;
 }
 return d;
}
let gid=0;
function lineSvg(o){ // o:{w,h,padL,padR,padT,padB,min,max,grid,glow,lw}
 const id='g'+(++gid);
 const {w,h}=o; const padL=o.padL??10,padR=o.padR??10,padT=o.padT??10,padB=o.padB??8;
 const data=[[0,80.2],[2,80.3],[4,80.4],[5,80.2],[6,79.9]];
 const X=i=>padL+i/6*(w-padL-padR), Y=v=>padT+(1-(v-o.min)/(o.max-o.min))*(h-padT-padB);
 const pts=data.map(([i,v])=>[X(i),Y(v)]);
 const d=smooth(pts), last=pts[pts.length-1];
 const area=d+` L${last[0]},${h-padB} L${pts[0][0]},${h-padB} Z`;
 const grid=(o.gridVals||[]).map(v=>`<line x1="${padL}" x2="${w-padR}" y1="${Y(v)}" y2="${Y(v)}" style="stroke:var(--grid)" stroke-width="1" ${o.dash?'stroke-dasharray="3 4"':''}/>`).join('');
 return `<svg width="${w}" height="${h}" viewBox="0 0 ${w} ${h}" style="display:block;overflow:visible">
 <defs><linearGradient id="${id}" x1="0" y1="0" x2="0" y2="1"><stop offset="0" style="stop-color:var(--lwTop)"/><stop offset="1" style="stop-color:var(--lwTop);stop-opacity:0"/></linearGradient>
 <filter id="${id}b" x="-20%" y="-50%" width="140%" height="200%"><feGaussianBlur stdDeviation="4"/></filter></defs>
 ${grid}
 <path d="${area}" fill="url(#${id})"/>
 ${o.glow?`<path d="${d}" fill="none" style="stroke:var(--lw)" stroke-width="7" stroke-linecap="round" opacity=".55" filter="url(#${id}b)"/>`:''}
 <path d="${d}" fill="none" style="stroke:var(--lw)" stroke-width="${o.lw||3}" stroke-linecap="round" stroke-linejoin="round"/>
 ${pts.slice(0,-1).map(p=>`<circle cx="${p[0]}" cy="${p[1]}" r="3" style="fill:var(--card2,var(--card));stroke:var(--lw)" stroke-width="2"/>`).join('')}
 <circle cx="${last[0]}" cy="${last[1]}" r="10" style="fill:var(--lw)" opacity=".18"/>
 <circle cx="${last[0]}" cy="${last[1]}" r="5.5" style="fill:var(--lw);stroke:var(--card2,var(--card))" stroke-width="2.5"/>
 ${o.extra?o.extra({X,Y,pts,last}):''}</svg>`;
}

function barsSvg(o){ // o:{w,h,vals,treat,kind:'f'|'s',avg,bw}
 const {w,h,vals}=o; const slot=w/vals.length, bw=o.bw||Math.min(14,slot-6);
 const max=o.max, base=h-1;
 const Y=v=>base-(v/max)*(h-6);
 let s='';
 vals.forEach((v,i)=>{
  const x=i*slot+(slot-bw)/2;
  const today=i===vals.length-1;
  const colVar=today?`var(--b${o.kind})`:`var(--b${o.kind}p)`;
  if(v==null){s+=`<rect x="${x+bw/2-3}" y="${base-3}" width="6" height="3" rx="1.5" style="fill:var(--b${o.kind}p)" opacity=".7"/>`;return;}
  const y=Y(v),r=Math.min(5,bw/2);
  const path=`M${x},${base} L${x},${y+r} Q${x},${y} ${x+r},${y} L${x+bw-r},${y} Q${x+bw},${y} ${x+bw},${y+r} L${x+bw},${base} Z`;
  if(today&&o.treat){
   const yt=Y(v-o.treat);
   s+=`<path d="${path}" style="fill:var(--bft)"/><rect x="${x}" y="${yt}" width="${bw}" height="${base-yt}" style="fill:var(--b${o.kind})"/>`;
  } else s+=`<path d="${path}" style="fill:${colVar}"/>`;
 });
 const ay=Y(o.avg);
 s+=`<line x1="0" x2="${w}" y1="${ay}" y2="${ay}" style="stroke:var(--avg)" stroke-width="1.2" stroke-dasharray="3 3"/>`;
 return `<svg width="${w}" height="${h}" viewBox="0 0 ${w} ${h}" style="display:block;overflow:visible"><line x1="0" x2="${w}" y1="${base+.5}" y2="${base+.5}" style="stroke:var(--grid)"/>${s}</svg>`;
}

const FOOD=[1030,950,null,1110,1020,875,679];
const STEPS=[6634,11800,10100,9400,8100,7000,4210];

const BASE_CSS=`
*{box-sizing:border-box;margin:0;padding:0}
body{background:#2a2c2e;font-family:'Roboto Flex','Roboto',sans-serif;-webkit-font-smoothing:antialiased}
.cap{height:28px;line-height:28px;padding:0 12px;font:500 12px/28px Roboto,sans-serif;color:#fff;background:#2a2c2e;letter-spacing:.2px}
.phone{width:360px;height:780px;position:relative;overflow:hidden;background:var(--bg);color:var(--tx)}
.ms{font-family:'Material Symbols Rounded';font-weight:400;line-height:1;display:inline-block;flex:none;user-select:none}
.top{height:64px;display:flex;align-items:center;padding:0 12px 0 16px;gap:4px}
.top .ttl{flex:1;font-size:22px;font-weight:500;letter-spacing:-.1px}
.top .ms{width:40px;text-align:center;color:var(--tx)}
.wrap{padding:0 16px;display:flex;flex-direction:column;gap:12px}
.row2{display:flex;gap:12px}
.card{border-radius:var(--r);padding:16px 20px 14px;position:relative}
.card.s{flex:1;min-width:0;padding:14px 14px 12px}
.cw{background:var(--cw);color:var(--tw)}.cf{background:var(--cf);color:var(--tf)}.cs{background:var(--cs);color:var(--ts)}
.cw .mu{color:var(--mw)}.cf .mu{color:var(--mf)}.cs .mu{color:var(--ms)}
.hd{display:flex;align-items:center;gap:8px;height:28px}
.hd .t{font-size:14px;font-weight:600;letter-spacing:.1px}
.hd .sub{font-size:12px}
.sp{flex:1}
.tile{width:28px;height:28px;border-radius:50%;display:flex;align-items:center;justify-content:center}
.cw .tile{background:var(--tileW);color:var(--tileWf)}.cf .tile{background:var(--tileF);color:var(--tileFf)}.cs .tile{background:var(--tileS);color:var(--tileSf)}
.dirB .tile{border-radius:10px}
.dirC .tile{width:20px;height:20px;background:transparent}
.dirC .hd{gap:6px}
.nrow{display:flex;align-items:baseline;gap:6px;margin-top:6px;white-space:nowrap}
.num{font-size:var(--numS);font-weight:var(--numW);line-height:1.05;letter-spacing:-1px;font-variant-numeric:tabular-nums}
.unit{font-size:18px;font-weight:500}
.s .num{font-size:calc(var(--numS)*.62);letter-spacing:-.5px}
.s .unit{font-size:13px}
.dirC .num{letter-spacing:-2px}
.dirC .s .num{letter-spacing:-1px;font-size:34px}
.delta{display:inline-flex;align-items:center;gap:2px;height:28px;padding:0 10px 0 6px;border-radius:14px;background:var(--pill);color:var(--pillTx);font-size:14px;font-weight:600;align-self:center;white-space:nowrap}
.mots{display:flex;gap:8px;margin-top:8px;white-space:nowrap}
.mot{font-size:13px;display:inline-flex;gap:5px;align-items:center}
.mot b{font-weight:600}
.dot{opacity:.5}
.subl{font-size:12.5px;margin-top:2px;height:17px;white-space:nowrap}
.ft{display:flex;align-items:center;gap:8px;margin-top:6px;white-space:nowrap}
.chip{display:inline-flex;align-items:center;height:30px;padding:0 6px 0 12px;border-radius:15px;border:1px solid var(--chipBd);background:var(--chipBg);font-size:13px;font-weight:500}
.s .chip{height:28px;padding:0 4px 0 10px;font-size:12.5px}
.rng{font-size:13px}
.dts{display:flex;justify-content:space-between;font-size:11px;margin-top:4px}
.fabs{position:absolute;left:16px;right:16px;bottom:22px;display:flex;gap:12px}
.btn{flex:1;height:56px;display:flex;align-items:center;justify-content:center;gap:8px;font-size:15px;font-weight:600;border-radius:calc(var(--r)*.8)}
.dirB .btn{border-radius:28px}
.b1{background:var(--fab);color:var(--fabTx);box-shadow:0 4px 14px rgba(0,0,0,.18)}
.b2{background:var(--fab2);color:var(--fab2Tx);border:1px solid var(--fab2Bd)}
.dirC .b1{box-shadow:0 0 22px color-mix(in srgb,var(--fab) 45%,transparent)}
`;
const FONTS=`<link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&family=Roboto+Flex:opsz,wght@8..144,400..800&family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@24,400,0..1,0&display=block" rel="stylesheet">`;

function page(caption,T,body,extraCss=''){
 return `<!doctype html><html><head><meta charset="utf-8">${FONTS}<style>${BASE_CSS}${extraCss}</style></head><body>
 <div class="cap">${caption}</div><div class="phone dir${T.dir}" style="${cssVars(T)}">${body}</div></body></html>`;
}

// ---------- front page ----------
function home(T){
 const C=T.dir==='C';
 const wMin=79.5,wMax=80.55;
 const wChart=lineSvg({w:288,h:C?150:136,min:wMin,max:wMax,padT:8,padB:10,glow:T.glow,lw:C?3:3,gridVals:[]});
 const fChart=barsSvg({w:130,h:96,vals:FOOD,treat:230,kind:'f',avg:944,max:1200,bw:13});
 const sChart=barsSvg({w:130,h:96,vals:STEPS,kind:'s',avg:8839,max:12500,bw:13});
 const chip=`<span class="chip">1 vk ${ic('arrow_drop_down',18)}</span>`;
 return `
 <div class="top"><div class="ttl">Terveyslappu</div>${ic('help',24)}${ic('more_vert',24)}</div>
 <div class="wrap">
  <div class="card cw">
   <div class="hd"><span class="tile">${ic('monitor_weight',C?20:18)}</span><span class="t">Paino</span><span class="sub mu">· tänään 7.05</span><span class="sp"></span>${ic('chevron_right',22,0,'opacity:.7')}</div>
   <div class="nrow"><span class="num">79,9</span><span class="unit mu">kg</span><span class="sp"></span><span class="delta">${ic('arrow_downward',16)}−0,3 kg</span></div>
   <div style="margin-top:14px">${wChart}</div>
   <div class="dts mu"><span>11.11.</span><span>17.11.</span></div>
   <div class="ft">${chip}<span class="sp"></span><span class="rng mu">80,2 → 79,9 kg</span></div>
  </div>
  <div class="row2">
   <div class="card s cf">
    <div class="hd"><span class="tile">${ic('restaurant',C?20:17)}</span><span class="t">Syöty</span><span class="sp"></span>${ic('chevron_right',20,0,'opacity:.7')}</div>
    <div class="nrow"><span class="num">679</span><span class="unit mu">kcal</span></div>
    <div class="subl" style="color:var(--subF);font-weight:500">herkkuja 230 kcal</div>
    <div style="margin-top:10px">${fChart}</div>
    <div class="dts mu"><span>11.11.</span><span>17.11.</span></div>
    <div class="ft">${chip}<span class="sp"></span><span class="mu" style="font-size:12px">ka 944 /pv</span></div>
   </div>
   <div class="card s cs">
    <div class="hd"><span class="tile">${ic('directions_walk',C?20:18)}</span><span class="t">Askeleet</span><span class="sp"></span>${ic('chevron_right',20,0,'opacity:.7')}</div>
    <div class="nrow"><span class="num">4 210</span><span class="unit mu">askelta</span></div>
    <div class="subl mu">tänään</div>
    <div style="margin-top:10px">${sChart}</div>
    <div class="dts mu"><span>11.11.</span><span>17.11.</span></div>
    <div class="ft">${chip}<span class="sp"></span><span class="mu" style="font-size:11.5px">ka 8 839 /pv</span></div>
   </div>
  </div>
 </div>`;
}

// ---------- historia (A) ----------
const HIST_CSS=`
.tabs{display:flex;height:44px;border-bottom:1px solid var(--grid)}
.tab{flex:1;display:flex;align-items:center;justify-content:center;font-size:14px;font-weight:500;color:var(--mu);position:relative}
.tab.on{color:var(--acc);font-weight:600}
.tab.on::after{content:'';position:absolute;bottom:0;left:50%;margin-left:-16px;width:32px;height:3px;border-radius:3px 3px 0 0;background:var(--lw)}
.seg{display:flex;margin:12px 16px 0;height:36px;border-radius:18px;background:var(--pill2);padding:3px;gap:2px}
.seg div{flex:1;border-radius:15px;display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:500;color:var(--mu)}
.seg div.on{background:var(--card);color:var(--acc);font-weight:600;box-shadow:0 1px 3px rgba(0,0,0,.12)}
.hc{margin:12px 16px 0;background:var(--card);border-radius:var(--r);padding:14px 12px 10px;position:relative}
.hc .rngt{font-size:12px;color:var(--mu);padding-left:4px;display:flex;justify-content:space-between}
.hero{margin:12px 16px 0;background:var(--cw);border-radius:var(--r);padding:14px 20px;display:flex;align-items:center}
.hero .lab{font-size:13px;color:var(--mu)}
.hero .big{font-size:36px;font-weight:600;letter-spacing:-.8px;display:flex;align-items:center;gap:2px;white-space:nowrap;line-height:1.1}
.hero .big small{font-size:18px;font-weight:500;color:var(--mu);margin-left:4px;letter-spacing:0}
.hero .side{margin-left:auto;text-align:right;font-size:13px;color:var(--mu)}
.hero .side b{display:block;font-size:17px;font-weight:600;color:var(--tx);white-space:nowrap;margin-top:2px}
.kv{margin:8px 16px 0;background:var(--card);border-radius:var(--r);padding:12px 8px;display:flex}
.kv div{flex:1;text-align:center;font-size:12px;color:var(--mu);white-space:nowrap}
.kv div b{display:block;font-size:15.5px;font-weight:600;color:var(--tx);margin-top:2px}
.kv div+div{border-left:1px solid var(--grid)}
.lst{margin:8px 16px 0;background:var(--card);border-radius:var(--r);padding:2px 4px 2px 18px}
.li{display:flex;align-items:center;height:52px;font-size:15px;border-bottom:1px solid var(--grid)}
.li:last-child{border:0}
.li .d{flex:1}
.li .ch{font-size:13px;color:var(--mu);display:flex;align-items:center;margin-right:12px;white-space:nowrap}
.li .kg{font-weight:600;font-size:16px;white-space:nowrap}
.li .ms.more{width:36px;text-align:center;color:var(--mu)}
.mk{position:absolute;background:var(--tx);color:var(--bg);border-radius:12px;padding:6px 10px;font-size:12px;font-weight:500;white-space:nowrap;line-height:1.25}
.mk b{font-size:14px;font-weight:600;display:block}
`;
function histogram(T){
 const w=304,h=176, padL=44,padR=14;
 const extra=({X,Y,last})=>`<line x1="${last[0]}" x2="${last[0]}" y1="6" y2="${h-22}" style="stroke:var(--lw)" stroke-dasharray="3 3" opacity=".6"/>`;
 const svg=lineSvg({w:w,h:h-22,padL:padL,padR:padR,padT:14,padB:10,min:79.4,max:81.6,gridVals:[81.5,80.5,79.5],dash:true,extra,lw:3});
 const yl=[81.5,80.5,79.5].map(v=>{const y=14+(1-(v-79.4)/2.2)*(h-22-24);return `<div style="position:absolute;left:0;top:${y-7}px;font-size:11px;color:var(--mu);width:42px;white-space:nowrap">${String(v).replace('.',',')} kg</div>`}).join('');
 const days=['Ke','To','Pe','La','Su','Ma','Ti'].map((d,i)=>{const x=padL+i/6*(w-padL-padR);return `<span style="position:absolute;left:${x-10}px;width:20px;text-align:center;font-size:11px;color:var(--mu)${i===6?';color:var(--tx);font-weight:600':''}">${d}</span>`}).join('');
 return `<div class="hc"><div class="rngt"><span>11.11.2026 – 17.11.2026</span></div>
  <div style="position:relative;margin-top:6px;height:${h-22}px">${yl}<div style="position:absolute;left:0;top:0">${svg}</div></div>
  <div style="position:relative;height:18px;margin-top:2px">${days}</div>
  <div class="mk" style="right:16px;top:34px"><span style="opacity:.7">Ti 17.11. 7.05</span><b>79,9 kg</b></div></div>`;
}
function historia(T,tab=0){
 const tabs=['Paino','BMI','Syöty','Askeleet','Kulutus'].map((t,i)=>`<div class="tab${i===tab?' on':''}">${t}</div>`).join('');
 const seg=['1 vk','1 kk','3 kk','1 v','Kaikki','Oma'].map((t,i)=>`<div${i===0?' class="on"':''}>${t}</div>`).join('');
 const rows=[['Tänään 7.05','↓','−0,3 kg','79,9 kg'],['Eilen 7.05','↓','−0,3 kg','80,2 kg'],['Su 15.11. 7.05','↑','+0,1 kg','80,4 kg'],['Pe 13.11. 7.05','↑','+0,1 kg','80,3 kg'],['Ke 11.11. 7.05','','','80,2 kg']]
  .map(([d,a,c,k])=>`<div class="li"><span class="d">${d}</span><span class="ch">${a?ic(a==='↓'?'arrow_downward':'arrow_upward',15)+c:''}</span><span class="kg">${k}</span>${ic('more_vert',20,0,'')}</div>`).join('').replace(/class="ms" style/g,'class="ms more" style');
 return `<div class="top" style="height:56px">${ic('arrow_back',24)}<div class="ttl" style="margin-left:4px">Historia</div>${ic('help',24)}${ic('more_vert',24)}</div>
 <div class="tabs">${tabs}</div>
 <div class="seg">${seg}</div>
 ${histogram(T)}
 <div class="hero"><div><div class="lab">Muutos</div><div class="big">${ic('arrow_downward',30,0,'')}−0,3<small>kg</small></div></div><div class="side">Viikossa<b>↓ −0,34 kg</b></div></div>
 <div class="kv"><div>Keskiarvo<b>80,2 kg</b></div><div>Alin<b>79,9 kg</b></div><div>Ylin<b>80,4 kg</b></div><div>Painoja<b>5</b></div></div>
 <div class="lst">${rows}</div>`;
}

function historiaFood(T){
 const w=304,padL=48,padR=10,h=150,days=['Ke','To','Pe','La','Su','Ma','Ti'];
 const plotW=w-padL-padR,slot=plotW/7;
 const bars=barsSvg({w:plotW,h:h,vals:FOOD,treat:230,kind:'f',avg:944,max:1500,bw:22});
 const gy=v=>h-1-(v/1500)*(h-6);
 const fmt=v=>String(v).replace(/\B(?=(\d{3})+$)/g,' ');
 const grid=[0,750,1500].map(v=>`<div style="position:absolute;left:0;top:${gy(v)-7+14}px;font-size:11px;color:var(--mu);width:44px;white-space:nowrap">${fmt(v)} kcal</div>`+(v?`<div style="position:absolute;left:${padL}px;right:${padR}px;top:${gy(v)+14}px;border-top:1px dashed var(--grid)"></div>`:'')).join('');
 const dl=days.map((d,i)=>`<span style="position:absolute;left:${padL+(i+.5)*slot-10}px;width:20px;text-align:center;font-size:11px;color:var(--mu)${i===6?';color:var(--tx);font-weight:600':''}">${d}</span>`).join('');
 const sw=c=>`<i style="display:inline-block;width:10px;height:10px;border-radius:3px;background:var(${c});margin-right:5px;vertical-align:-1px"></i>`;
 const chart=`<div class="hc"><div class="rngt"><span>11.11.2026 – 17.11.2026</span></div>
  <div style="position:relative;margin-top:6px;height:${h+14}px">${grid}<div style="position:absolute;left:${padL}px;top:14px">${bars}</div></div>
  <div style="position:relative;height:18px;margin-top:4px">${dl}</div>
  <div style="display:flex;gap:14px;font-size:11.5px;color:var(--mu);padding:6px 4px 0"><span>${sw('--bf')}syöty</span><span>${sw('--bft')}herkut</span><span>- - keskiarvo</span></div>
  <div class="mk" style="right:16px;top:44px"><span style="opacity:.7">Ti 17.11.</span><b>679 kcal</b></div></div>`;
 const base=historia(T,2);
 const more=ic('more_vert',20,0,'').replace('class="ms"','class="ms more"');
 const rows=[['Tänään','herkkuja 230 kcal','679 kcal'],['Eilen','','875 kcal'],['Su 15.11.','','1 020 kcal'],['La 14.11.','','1 110 kcal']]
  .map(([d,t,k])=>`<div class="li"><span class="d">${d}</span><span class="ch" style="color:var(--bft)">${t}</span><span class="kg">${k}</span>${more}</div>`).join('');
 return base.replace(/<div class="hc">[\s\S]*$/, chart+`
 <div class="hero" style="background:var(--cf2)"><div><div class="lab">Keskimäärin</div><div class="big">944<small>kcal/pv</small></div></div></div>
 <div class="kv"><div>Herkut<b>—</b></div><div>Päiviä<b>6 / 7</b></div></div>
 <div class="lst">${rows}</div>`);
}

// ---------- weigh-in (unused) ----------
const WI_CSS=`
.scrim{position:absolute;inset:0;background:var(--scrim)}
.dlg{position:absolute;left:24px;right:24px;top:218px;background:var(--dlg);border-radius:28px;padding:28px 24px 16px;text-align:center;box-shadow:0 8px 32px rgba(0,0,0,.28)}
.ck{width:72px;height:72px;margin:0 auto;border-radius:50%;background:var(--accBg);display:flex;align-items:center;justify-content:center;position:relative}
.ck i{width:52px;height:52px;border-radius:50%;background:var(--lw);display:flex;align-items:center;justify-content:center;color:var(--ckTx)}
.wbig{font-size:52px;font-weight:600;letter-spacing:-1.2px;margin-top:14px;line-height:1.05;white-space:nowrap}
.wbig small{font-size:22px;font-weight:500;color:var(--mu);margin-left:6px;letter-spacing:0}
.saved{font-size:15px;font-weight:600;color:var(--acc);margin-top:6px}
.ctx{margin-top:18px;background:var(--bg);border-radius:16px;padding:12px 14px 12px;text-align:left}
.ctx .l1{font-size:12.5px;color:var(--mu);display:flex;justify-content:space-between;white-space:nowrap}
.ctx .l1 b{color:var(--tx);font-weight:600}
.track{position:relative;height:8px;border-radius:4px;background:var(--grid);margin:12px 0 8px}
.track .band{position:absolute;top:0;bottom:0;border-radius:4px;background:var(--lw);opacity:.35}
.track .mark{position:absolute;top:-4px;width:16px;height:16px;border-radius:50%;background:var(--lw);border:3px solid var(--dlg);margin-left:-8px}
.ctx .l2{font-size:13px;color:var(--tx)}
.acts{display:flex;justify-content:flex-end;align-items:center;gap:8px;margin-top:16px}
.tbtn{height:44px;padding:0 16px;display:flex;align-items:center;font-size:14.5px;font-weight:600;color:var(--acc);border-radius:22px}
.fbtn{height:44px;padding:0 26px;display:flex;align-items:center;font-size:14.5px;font-weight:600;background:var(--fab);color:var(--fabTx);border-radius:22px}
`;
function weighin(T,dark){
 const dlg=dark?'#212625':'#FAFBFA', ckTx=dark?'#07251E':'#fff';
 // band 79,7–80,6 on track 79,4..81,0 ; marker 80,4
 const f=v=>((v-79.4)/1.6*100).toFixed(1);
 return `<div style="position:absolute;inset:0;--card2:var(--card)">${home(T)}</div><div class="scrim"></div>
 <div class="dlg" style="--dlg:${dlg};--ckTx:${ckTx}">
  <div class="ck"><i>${ic('check',34,1,'font-variation-settings:\'FILL\' 0,\'wght\' 600')}</i></div>
  <div class="wbig">80,4<small>kg</small></div>
  <div class="saved">Tallennettu</div>
  <div class="ctx">
   <div class="l1"><span>Viikon vaihteluväli</span><b>79,7–80,6 kg</b></div>
   <div class="track"><div class="band" style="left:${f(79.7)}%;width:${f(80.6)-f(79.7)}%"></div><div class="mark" style="left:${f(80.4)}%"></div></div>
   <div class="l2">Tämä on tavallisen vaihtelun sisällä.</div>
  </div>
  <div class="acts"><div class="tbtn">Poista</div><div class="fbtn">Valmis</div></div>
 </div>`;
}

// ---------- render ----------
const frames=[];
function add(file,caption,T,body,css=''){frames.push({file,html:page(caption,T,body,css)});}
const names={'A':'A · Rauhallinen harmaa + teal','B':'B · Värikoodatut kortit','C':'C · Tumma graniitti + hohtava aksentti'};
const modeFi={light:'vaalea',dark:'tumma'};
for(const [k,T] of Object.entries(TK)){
 const [d,m]=k.split('-');
 add(`${d}-etusivu-${m}`,`${d} · Etusivu · ${modeFi[m]}`,T,home(T));
}
for(const m of ['light','dark']){
 const T={...TK['A-'+m],cf2:m==='light'?'#F7ECD6':'#2A2417',pill2:m==='light'?'#E3E8E6':'#222827',mu:m==='light'?'#586661':'#9BAAA5'};
 T.mu=m==='light'?'#586661':'#9BAAA5';
 add(`A-historia-paino-${m}`,`A · Historia · Paino · ${modeFi[m]}`,T,historia(T),HIST_CSS);
 add(`A-historia-syoty-${m}`,`A · Historia · Syöty · ${modeFi[m]}`,T,historiaFood(T),HIST_CSS);
}

(async()=>{
 const b=await chromium.launch();
 const ctx=await b.newContext({viewport:{width:360,height:808},deviceScaleFactor:2});
 const p=await ctx.newPage();
 for(const f of frames){
  fs.writeFileSync(OUT+f.file+'.html',f.html);
  await p.setContent(f.html,{waitUntil:'networkidle'});
  await p.evaluate(()=>document.fonts.ready);
  await p.waitForTimeout(250);
  await p.screenshot({path:OUT+f.file+'.png',clip:{x:0,y:0,width:360,height:808}});
  // density 1 check copy
 }
 // overviews
 const ov=async(name,l,r)=>{
  const html=`<!doctype html><html><head><meta charset="utf-8"><style>*{margin:0}body{background:#2a2c2e;display:flex;gap:16px;padding:16px;width:776px;font-family:Roboto,sans-serif}
  .f{width:360px}.f .c{color:#fff;font:500 12px/28px Roboto,sans-serif;height:28px;padding:0 4px}.f img{width:360px;height:780px;display:block;border-radius:0}</style></head><body>
  ${[l,r].map(x=>`<div class="f"><div class="c">${x.cap}</div><img src="file://${x.src}"></div>`).join('')}</body></html>`;
  fs.writeFileSync(OUT+name+'.html',html);
  const pg=await ctx.newPage();await pg.setViewportSize({width:776,height:868});
  await pg.goto('file://'+OUT+name+'.html');await pg.waitForTimeout(300);
  await pg.screenshot({path:OUT+name+'.png',clip:{x:0,y:0,width:776,height:868}});await pg.close();
 };
 const cur={cap:'Nykyinen · Etusivu · vaalea',src:SHOTS+'01-home-light.png'};
 const curD={cap:'Nykyinen · Etusivu · tumma',src:SHOTS+'01-home-dark.png'};
 const crop=x=>({cap:x.cap,src:OUT+x.file+'-body.png'});
 // body-only crops (without caption) of the new frames
 const body=async(file)=>{await p.setContent(frames.find(f=>f.file===file).html,{waitUntil:'networkidle'});await p.evaluate(()=>document.fonts.ready);await p.waitForTimeout(250);
  await p.screenshot({path:OUT+file+'-body.png',clip:{x:0,y:28,width:360,height:780}});};
 for(const f of ['A-etusivu-light','A-etusivu-dark','B-etusivu-light','B-etusivu-dark','C-etusivu-dark','C-etusivu-light']) await body(f);
 await ov('overview-nykyinen-vs-A',cur,{cap:'A · Etusivu · vaalea',src:OUT+'A-etusivu-light-body.png'});
 await ov('overview-nykyinen-vs-B',cur,{cap:'B · Etusivu · vaalea',src:OUT+'B-etusivu-light-body.png'});
 await ov('overview-nykyinen-vs-C',cur,{cap:'C · Etusivu · vaalea',src:OUT+'C-etusivu-light-body.png'});
 await ov('overview-nykyinen-vs-A-tumma',curD,{cap:'A · Etusivu · tumma',src:OUT+'A-etusivu-dark-body.png'});
 await ov('overview-nykyinen-vs-B-tumma',curD,{cap:'B · Etusivu · tumma',src:OUT+'B-etusivu-dark-body.png'});
 await ov('overview-nykyinen-vs-C-tumma',curD,{cap:'C · Etusivu · tumma',src:OUT+'C-etusivu-dark-body.png'});
 await b.close();
})().catch(e=>{console.error(e);process.exit(1)});
