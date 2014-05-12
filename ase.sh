( groovy asespotter.groovy > ~/ase.txt ; grep "\[" ~/ase.txt ; zenity --info --text 'Done') &
sleep 5
tail -f ~/ase.txt
