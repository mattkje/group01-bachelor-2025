export interface License {
    id: number;
    name: string;
}

export interface Zone {
    id: number;
    name: string;
    capacity: number;
    isPickerZone: boolean;
    pickerTasks: PickerTask[];
    tasks: Task[];
    workers: Worker[];
}

export interface Task {
    id: number;
    name: string;
    description: string;
    minWorkers: number;
    maxWorkers: number;
    requiredLicense: License[];
    zoneId: number;
    maxTime: number;
    minTime: number;
}

export interface ActiveTask {
    id: number;
    dueDate: string;
    startTime: string;
    endTime: string;
    eta: string;
    date: string;
    task: Task;
    workers: Worker[];
    strictStart: boolean;
}

export interface Worker {
    id: number;
    name: string;
    zone: number;
    efficiency: number;
    availability: boolean;
    breakStartTime: string;
    dead: boolean;
    licenses: License[];
    workerType: string;
}

export interface TimeTable {
    id: number;
    startTime: string;
    endTime: string;
    realStartTime: string;
    realEndTime: string;
    workerId: number;
}

export interface PickerTask {
    id: number;
    distance: number;
    packAmount: number;
    linesAmount: number;
    weight: number;
    volume: number;
    avgHeight: number;
    time: number;
    startTime: string;
    endTime: string;
    date: string;
    zoneId: number;
    worker: Worker;
}
