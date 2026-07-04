import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import heroImg from './assets/hero.png'

import './App.css'

const ColorfulComponent = ({color}) => (
  <div style= {{color: color }}>
  <p>This componentis {color}.</p>
</div>
);


function App() {

  return (
    <div>
      <ColorfulComponent color="blue" />
      <ColorfulComponent color="red" />
      <ColorfulComponent color="green" />
    </div>
  );
}

export default App
