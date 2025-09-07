
export const fadeUp = (delay=0)=>({ initial:{opacity:0, y:10}, animate:{opacity:1, y:0, transition:{delay, duration:.35}} });
export const fade = (delay=0)=>({ initial:{opacity:0}, animate:{opacity:1, transition:{delay, duration:.35}} });
