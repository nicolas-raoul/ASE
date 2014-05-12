( groovy asespotter.groovy > ~/t/ase.txt ; grep "\[" ~/t/ase.txt ; zenity --info --text 'Done') &
sleep 5
tail -f ~/ase.txt
